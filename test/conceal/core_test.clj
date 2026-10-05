(ns conceal.core-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [conceal.core :refer [conceal reveal mk-opts base64-encode base64-decode]]))

(def opts (mk-opts "text-to-be-encrypted" "12345678"))

(deftest can-encrypt-decrypt
  (is (= "text-to-be-encrypted"
         (->> opts
              conceal
              (assoc opts :input)
              reveal))))

(deftest encryption-uses-fresh-randomness
  (let [first-ciphertext (conceal opts)
        second-ciphertext (conceal opts)]
    (is (not= first-ciphertext second-ciphertext))
    (is (= "text-to-be-encrypted"
           (reveal (assoc opts :input first-ciphertext))))))

(deftest tampered-ciphertext-is-rejected
  (let [ciphertext (conceal opts)
        payload (-> ciphertext
                    (subs 3)
                    base64-decode)
        last-index (dec (alength payload))
        _ (aset-byte payload last-index
                     (unchecked-byte (bit-xor 1 (aget payload last-index))))
        tampered (str "v1:" (base64-encode payload))]
    (is (thrown? javax.crypto.AEADBadTagException
                 (reveal (assoc opts :input tampered))))))

(comment
  *e
  opts
  (run-tests)
  "see https://github.com/clojure-expectations/clojure-test for examples")
