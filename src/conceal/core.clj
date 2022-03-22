(ns conceal.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:import [javax.crypto
            KeyGenerator
            SecretKey
            SecretKeyFactory
            Cipher]
           [java.security SecureRandom]
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
  "Crete a crypto initialization vector."
  (delay
   (-> [100 -48 47 -50 90 78 -94 127 -5 -120 -10 -7 63 63 92 99]
       byte-array
       IvParameterSpec.)))

(defn base64-encode [text]
  (-> (Base64/getEncoder)
      (.encodeToString text)))

(defn base64-decode [text]
  (-> (Base64/getDecoder)
      (.decode text)))

(defn ^String conceal [opts]
  (let [{:keys [algorithm input key i-vec]} opts
        cipher (Cipher/getInstance algorithm)]
    (.init cipher Cipher/ENCRYPT_MODE key i-vec)
    (-> cipher
        (.doFinal (.getBytes input))
        base64-encode)))

(defn ^String reveal [opts]
  (let [{:keys [algorithm cipher-text key i-vec]} opts]
    (-> (doto (Cipher/getInstance algorithm)
          (.init Cipher/DECRYPT_MODE key i-vec))
        (.doFinal (base64-decode cipher-text))
        String.)))

(defn -main [& args] (println "hello world"))

(comment
  *e
  @init-vec

  (def key (key-from-pass "pass" "12345678"))

  (def cipher-text
    (conceal
     {:algorithm "AES/CBC/PKCS5Padding"
      :input     "foo-bar"
      :key       key
      ;;:key (gen-key 128)
      :i-vec     @init-vec}))

  cipher-text

  (reveal {:algorithm "AES/CBC/PKCS5Padding"
           :cipher-text cipher-text
           :key         key
           :i-vec       @init-vec})

  ;;
  )