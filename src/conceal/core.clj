(ns conceal.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:import [javax.crypto
            SecretKey
            SecretKeyFactory
            Cipher]
           [javax.crypto.spec
            PBEKeySpec
            SecretKeySpec 
            GCMParameterSpec] 
           [java.nio.charset StandardCharsets] 
           [java.security SecureRandom] 
           [java.util Base64 Arrays]))

(def ^:private kdf-iterations 600000)
(def ^:private salt-length 16)
(def ^:private iv-length 12)
(def ^:private gcm-tag-bits 128)
(def ^:private envelope-prefix "v1:")
(def ^:private secure-random (SecureRandom.))

(defn key-from-pass
  "Derive an AES-256 key from a password and a unique salt (byte array or string)."
  ^SecretKey [^String pass salt]
  (let [salt-bytes (if (string? salt)
                     (.getBytes ^String salt StandardCharsets/UTF_8)
                     salt)
        password-chars (.toCharArray pass)
        spec (PBEKeySpec. password-chars ^bytes salt-bytes kdf-iterations 256)]
    (try
      (let [factory (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA256")]
        (SecretKeySpec. (.getEncoded (.generateSecret factory spec)) "AES"))
      (finally
        (.clearPassword spec)
        (Arrays/fill password-chars (char 0))))))

(defn- random-bytes [length]
  (let [bytes (byte-array length)]
    (.nextBytes secure-random bytes)
    bytes))

(defn mk-opts
  "Create a default map of crypto options suitable for use with conceal/reveal.
  The password is used to derive a key with a per-message random salt."
  [input pass]
  {:input input
  :pass pass})

(defn base64-encode [text]
  (-> (Base64/getEncoder)
      (.encodeToString text)))

(defn base64-decode [text]
  (-> (Base64/getDecoder)
      (.decode text)))

(defn conceal
  "Encrypt a string using password-derived AES-256-GCM.
   Returns a versioned Base64 envelope."
  ^String [opts]
  (let [{:keys [input pass]} opts
        salt (random-bytes salt-length)
        iv (random-bytes iv-length)
        cipher (Cipher/getInstance "AES/GCM/NoPadding")]
    (.init cipher Cipher/ENCRYPT_MODE (key-from-pass pass salt)
           (GCMParameterSpec. gcm-tag-bits iv))
    (str envelope-prefix
         (base64-encode
          (byte-array
           (concat salt iv
                   (.doFinal cipher (.getBytes ^String input StandardCharsets/UTF_8))))))))

(defn reveal
  "Decrypt a versioned envelope produced by conceal; authentication failures are rejected."
  ^String [opts]
  (let [{:keys [input pass]} opts]
    (when-not (.startsWith ^String input envelope-prefix)
      (throw (IllegalArgumentException. "Unsupported ciphertext format")))
    (let [payload (base64-decode (subs input (count envelope-prefix)))
          minimum-length (+ salt-length iv-length 16)]
      (when (< (alength ^bytes payload) minimum-length)
        (throw (IllegalArgumentException. "Ciphertext envelope is truncated")))
      (let [salt (Arrays/copyOfRange ^bytes payload 0 salt-length)
            iv (Arrays/copyOfRange ^bytes payload salt-length (+ salt-length iv-length))
            ciphertext (Arrays/copyOfRange ^bytes payload (+ salt-length iv-length)
                                           (alength ^bytes payload))
            cipher (Cipher/getInstance "AES/GCM/NoPadding")]
        (.init cipher Cipher/DECRYPT_MODE (key-from-pass pass salt)
               (GCMParameterSpec. gcm-tag-bits iv))
        (String. (.doFinal cipher ciphertext) StandardCharsets/UTF_8)))))

(defn key-from-env
  "Lookup crypto key password from env variable CONCEAL_KEY.
   If the variable is not set an exception is thrown."
  []
  (if-let [key (System/getenv "CONCEAL_KEY")]
    key
    (throw (AssertionError.
            "Expected key to be found in env var CONCEAL_KEY"))))

(def cli-options
  [["-c" "--conceal-text text-to-encrypt" "Encrypt a string"]
   ["-r" "--reveal-text text-to-decrypt" "Decrypt a string"]
   ["-h" "--help"]])

(defn run
  "Execute the appropriate action based on command-line args."
  [options]
  (let [{:keys [conceal-text reveal-text]} options]
    (cond
      conceal-text (println (->> (key-from-env)
                                 (mk-opts conceal-text)
                                 conceal))
      reveal-text (println (-> reveal-text
                               (mk-opts (key-from-env))
                               reveal)))))

(defn -main [& args]
  (let [{:keys [options
                #_arguments
                summary
                errors]} (parse-opts args cli-options)]
    (cond
      errors               (println errors)
      (or (empty? options)
          (:help options)) (println summary)
      :else                (run options))))

(comment
  *e
  (key-from-env)
  (->> "key-to-encrypt-decrypt"
       (mk-opts "text to encrypt")
       conceal)

  (-> "v1:U0/shSpjIHngxzh6W3uWTjBGvwImuxeYoTPTJ8HI2ttmm6tUiVIG7DSjMG0i25yGRa/BHAkTqkXVTlo="
      (mk-opts "key-to-encrypt-decrypt")
      reveal)

  (time (-> "v1:..."
            (mk-opts "key-to-encrypt-decrypt")
            reveal))

  ;;
  )