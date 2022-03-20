(ns conceal.core-test
  (:require [clojure.test :refer [use-fixtures run-tests]]
            [expectations.clojure.test :refer [defexpect
                                               expect expecting]]))

(defn setup [f] (f))

(use-fixtures :once setup)

(defexpect fix-me-I-fail (expect 1 0))

(comment
  *e
  (run-tests)
  "see https://github.com/clojure-expectations/clojure-test for examples")
