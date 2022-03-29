# conceal
verb - keep from sight; hide.


Utility for concealing small amounts of text.

Usage:
```clojure
(ns an.example 
  (:require [conceal.core :refer [reveal conceal mk-opts]]))

;;  
;; Encrypt
;;
(->> "key-to-encrypt-decrypt"
     (mk-opts "text to encrypt")
     conceal) 
;;
;; Decrypt
;;       
(-> "aOoOhYZ9S4Kr0iTW900NZQ=="
    (mk-opts "key-to-encrypt-decrypt")
    reveal) 
```
