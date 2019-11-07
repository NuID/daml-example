(ns example.utils
  (:require
   [nuid.cryptography :as crypt]
   [nuid.base64 :as base64]
   [nuid.codec :as codec]
   [nuid.zk :as zk]
   #?@(:clj
       [[clojure.spec-alpha2.gen :as gen]
        [clojure.spec-alpha2 :as s]
        [clojure.walk :as walk]
        [nuid.codec.multicodec :as multicodec]]
       :cljs
       [[clojure.spec.gen.alpha :as gen]
        [clojure.test.check.generators]
        [clojure.spec.alpha :as s]])))

(defn generate-default-parameters
  []
  {:curve    {:id "secp256k1"}
   :protocol {:id "knizk"}
   :hashfn   {:id                 "sha256"
              :normalization-form "NFKC"}
   :keyfn    {:id                 "sha256"
              :salt               (gen/generate (s/gen ::crypt/salt))
              :normalization-form "NFKC"}})

(defn generate-proof
  [{:keys [secret]}]
  (let [params (second (s/conform ::zk/parameters (generate-default-parameters)))
        pub    {:pub (zk/pub (merge params {:secret secret}))}
        nonce  {:nonce (gen/generate (s/gen ::crypt/nonce))}
        proof  (zk/proof (merge params pub nonce {:secret secret}))]
    (merge params pub nonce proof)))

#?(:clj
   (defn encode-credential
     [credential]
     (->>
      (s/conform ::zk/credential credential)
      (s/unform ::zk/credential)
      (walk/stringify-keys)
      (codec/encode "application/cbor")
      (multicodec/prefixed :cbor)
      (base64/encode))))

#?(:clj
   (defn decode-credential
     [encoded]
     (->>
      (base64/decode encoded)
      (multicodec/unprefixed)
      (codec/decode "application/cbor")
      (walk/keywordize-keys)
      (s/conform ::zk/credential))))
