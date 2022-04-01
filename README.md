# conceal
verb - keep from sight; hide.


Utility for concealing small amounts of text.

Usage as a library:
```clojure
;; In deps.edn, add this to your :deps map:
com.github.tstout/conceal
    {:git/url "https://github.com/tstout/conceal"
     :git/tag "v1.0.0"
     :git/sha "e9ab405"}


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
Add this to your ~/.clojure/deps.edn
```clojure
;; In your :aliases map:
:conceal {:extra-deps {com.github.tstout/conceal
                        {:git/url "https://github.com/tstout/conceal"
                         :git/tag "v1.0.0"
                         :git/sha "e9ab405"}}
          :main-opts ["-m" "conceal.core"]}

```
```
export CONCEAL_KEY=8675309

clj -M:conceal -c secret-text
u15arZvE/9IReo5nWHFb3A==

clj -M:conceal -r u15arZvE/9IReo5nWHFb3A==
secret-text

```