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
  "Generate a key form a password and a salt value."
  [^String pass ^String salt]
  (let [factory (SecretKeyFactory/getInstance "PBKDF2WithHmacSHA256")
        spec (PBEKeySpec. (.toCharArray pass) (.getBytes salt) 65536 256)]
    (SecretKeySpec. (.getEncoded (.generateSecret factory spec)) "AES")))

(def init-vec
  "A crypto initialization vector"
  (delay
   (-> [100 -48 47 -50 90 78 -94 127 -5 -120 -10 -7 63 63 92 99]
       byte-array
       IvParameterSpec.)))

(defn mk-opts
  "Create a map of crypto options suitable for use with conceal/reveal"
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
  "Perform a symmetric encryption of a string. Set mk-opts to create
   crypto options."
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

(defn -main [& args] (println "hello world"))

(comment
  *e
  @init-vec

  (def cipher-text
    (conceal (mk-opts "foo-bar" "5713853")))

  cipher-text

  (reveal (mk-opts cipher-text "5713853"))

  (time (reveal (mk-opts cipher-text "5713853")))
  ;;
  )