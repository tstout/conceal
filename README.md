# conceal
verb - keep from sight; hide.


Utility for concealing small amounts of text.

Usage as a library:
```clojure
;; In deps.edn 
{}


;;In an/example.clj
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
Usage via command line:
```
export CONCEAL_KEY=8675309

clj -M:conceal -c secret-text
u15arZvE/9IReo5nWHFb3A==

clj -M:conceal -r u15arZvE/9IReo5nWHFb3A==
secret-text

```