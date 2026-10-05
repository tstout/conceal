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

;; Encrypt and decrypt. Each call to conceal creates a fresh random salt
;; and AES-GCM nonce; the returned v1:... value contains both with the ciphertext.
(let [password "a-long-random-password"
    ciphertext (conceal (mk-opts "text to encrypt" password))]
  (reveal (mk-opts ciphertext password)))
```

The password is processed with PBKDF2-HMAC-SHA256 (600,000 iterations) to derive
an AES-256 key. Encryption uses AES-GCM with a fresh 16-byte salt and 12-byte
nonce per message. Keep passwords strong and secret. Ciphertexts created by
versions before this format change (unprefixed CBC ciphertexts) are not
compatible and cannot be decrypted by this version.
Usage via command line:
Add this to your ~/.clojure/deps.edn
```clojure
;; In your :aliases map:
:conceal {:extra-deps {com.github.tstout/conceal
                        {:git/url "https://github.com/tstout/conceal"
                         :git/tag "v2.0.0"
                         :git/sha "411d5f6"}}
          :main-opts ["-m" "conceal.core"]}

```
In your shell environment:
```
export CONCEAL_KEY=8675309
```

conceal (encrypt)
```
clj -M:conceal -c secret-text
v1:<randomized-base64-envelope>
```

reveal (decrypt)
```
clj -M:conceal -r 'v1:<randomized-base64-envelope>'
secret-text
```
