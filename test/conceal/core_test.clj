(ns conceal.core-test
  (:require [clojure.test :refer [run-tests]]
            [conceal.core :refer [conceal reveal mk-opts]]
            [expectations.clojure.test :refer [defexpect
                                               expect]]))

(def opts (mk-opts "text-to-be-encrypted" "12345678"))

(defexpect can-encrypt-decrypt
  (expect "text-to-be-encrypted"
          (->> opts
               conceal
               (assoc opts :input)
               reveal)))

(comment
  *e
  opts
  (run-tests)
  "see https://github.com/clojure-expectations/clojure-test for examples")
