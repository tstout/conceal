(ns conceal.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:import [javax.crypto
            SecretKey
            SecretKeyFactory
            Cipher]
           [javax.crypto.spec
            PBEKeySpec
            SecretKeySpec
            IvParameterSpec]
           [java.util Base64]))

(defn ^SecretKey key-from-pass
  "Generate a key from a password and a salt value."
  [^String pass ^String salt]
  (let [factory (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA256")
        spec (PBEKeySpec. (.toCharArray pass) (.getBytes salt) 65536 256)]
    (SecretKeySpec. (.getEncoded (.generateSecret factory spec)) "AES")))

(def init-vec
  "A default crypto initialization vector"
  (delay
   (-> [100 -48 47 -50 90 78 -94 127 -5 -120 -10 -7 63 63 92 99]
       byte-array
       IvParameterSpec.)))

(defn mk-opts
  "Create a default map of crypto options suitable for use with conceal/reveal.
   Feel free to merge/assoc/update as desired."
  [input pass]
  {:algorithm "AES/CBC/PKCS5Padding"
   :input     input
   :key       (key-from-pass pass "8675309")
   :i-vec     @init-vec})

(defn base64-encode [text]
  (-> (Base64/getEncoder)
      (.encodeToString text)))

(defn base64-decode [text]
  (-> (Base64/getDecoder)
      (.decode text)))

(defn ^String conceal
  "Perform a symmetric encryption of a string. Use mk-opts to create
   default crypto options and customize as desired."
  [opts]
  (let [{:keys [algorithm input key i-vec]} opts
        cipher (Cipher/getInstance algorithm)]
    (.init cipher Cipher/ENCRYPT_MODE key i-vec)
    (-> cipher
        (.doFinal (.getBytes input))
        base64-encode)))

(defn ^String reveal
  "Decrypt a string. See mk-opts for creating crypto options."
  [opts]
  (let [{:keys [algorithm input key i-vec]} opts]
    (-> (doto (Cipher/getInstance algorithm)
          (.init Cipher/DECRYPT_MODE key i-vec))
        (.doFinal (base64-decode input))
        String.)))

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
  @init-vec

  (key-from-env)
  (->> "key-to-encrypt-decrypt"
       (mk-opts "text to encrypt")
       conceal)

  (-> "aOoOhYZ9S4Kr0iTW900NZQ=="
      (mk-opts "key-to-encrypt-decrypt")
      reveal)

  (time (-> "aOoOhYZ9S4Kr0iTW900NZQ=="
            (mk-opts "key-to-encrypt-decrypt")
            reveal))

  ;;
  )