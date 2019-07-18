(ns example.utils
  (:require
   [nuid.elliptic.curve.point :as point]
   [nuid.cryptography :as crypt]
   [nuid.base64 :as base64]
   [nuid.zk :as zk]
   #?@(:clj
       [[nuid.multicodec :as multicodec]
        [nuid.cbor :as nuid.cbor]
        [clj-cbor.core :as cbor]
        [clojure.walk :as walk]])))

(def default-spec
  {:curve {:id "secp256k1"}
   :protocol {:id "knizk"}
   :hashfn {:id "sha256"
            :normalization-form "NFKC"}})

(defn create-proof
  [{:keys [secret]}]
  (let [secret {:secret secret}
        keyfn {:keyfn {:id "sha256"
                       :salt (crypt/secure-random-base64 32)
                       :normalization-form "NFKC"}}
        spec (merge default-spec keyfn)
        pub {:pub (zk/pub (zk/coerce (merge spec secret)))}
        nonce {:nonce (crypt/secure-random-bn 32)}
        proof (zk/proof (zk/coerce (merge spec pub nonce secret)))]
    (merge spec pub nonce proof)))

#?(:clj
   (def codec
     (cbor/cbor-codec
      {:read-handlers
       (merge cbor/default-read-handlers
              nuid.cbor/tagged-literal-read-handler)
       :write-handlers
       (merge cbor/default-write-handlers
              point/cbor-write-handler)})))

#?(:clj
   (defn decode
     [encoded]
     (->> (base64/decode encoded)
          (multicodec/unprefixed)
          (cbor/decode codec)
          (walk/keywordize-keys))))

#?(:clj
   (defn encode
     [params]
     (as-> params $
       (walk/stringify-keys $)
       (cbor/encode codec $)
       (multicodec/prefixed
        {:multiformats.codec/key :cbor
         :multiformats.codec/raw $})
       (base64/encode $))))
