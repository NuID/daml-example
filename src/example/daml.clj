(ns example.daml
  (:require
   [example.utils :as utils])
  (:import
   [com.daml.ledger.rxjava DamlLedgerClient]
   [com.nuid.credential.model.credential
    Credential
    Credential$Contract
    Credential$ContractId]
   [com.daml.ledger.javaapi.data
    CreatedEvent
    ArchivedEvent
    InclusiveFilter
    ExerciseCommand
    CreateCommand
    LedgerOffset
    LedgerOffset$Absolute
    LedgerOffset$LedgerBegin
    LedgerOffset$LedgerEnd
    Identifier
    FiltersByParty
    TransactionFilter]
   [java.util Optional Collections UUID]
   [java.time Instant]))

(def app-id "CredentialApp")
(defonce client-atom (atom nil))
(defonce ledger-offset-atom (atom nil))
(defonce credentials-atom (atom {}))
(defonce contracts-atom (atom {}))

(defn filter-for
  [{:keys [daml/template-id daml/party]}]
  (let [inclusive-filter (InclusiveFilter. (Collections/singleton template-id))
        filter (Collections/singletonMap party inclusive-filter)]
    (FiltersByParty. filter)))

(defn wrap-consumer
  [f]
  (reify java.util.function.Consumer
    (accept [this x]
      (f x))))

(defn wrap-reactive-consumer
  [f]
  (reify io.reactivex.functions.Consumer
    (accept [this x]
      (f x))))

(defn update-credentials-atom
  [response]
  (let [f #(reset! ledger-offset-atom (LedgerOffset$Absolute. %))]
    (.. response getOffset (ifPresent (wrap-consumer f)))
    (doseq [e (.getCreatedEvents response)]
      (let [contract (Credential$Contract/fromCreatedEvent e)
            id (.-id (.-data contract))
            credential (utils/decode (.-credential (.-data contract)))]
        (swap! credentials-atom assoc id credential)
        (swap! contracts-atom assoc (.-id contract) id)))))

(defn populate-credentials-atom
  [filter]
  (let [handler (wrap-reactive-consumer update-credentials-atom)]
    (.. @client-atom
        getActiveContractSetClient
        (getActiveContracts filter true)
        (blockingForEach handler))))

(defn process-created-event
  [e]
  (let [contract (Credential$Contract/fromCreatedEvent e)
        id (.-id (.-data contract))
        credential (utils/decode (.-credential (.-data contract)))]
    (swap! credentials-atom assoc id credential)
    (swap! contracts-atom assoc (.-id contract) id)))

(defn process-archived-event
  [e]
  (let [cid (Credential$ContractId. (.getContractId e))
        id (@contracts-atom cid)]
    (swap! credentials-atom dissoc id)
    (swap! contracts-atom dissoc cid)))

(defn process-transaction
  [transaction]
  (doseq [e (.getEvents transaction)]
    (cond (instance? CreatedEvent e)  (process-created-event e)
          (instance? ArchivedEvent e) (process-archived-event e))))

(defn start-transaction-listener
  [filter]
  (let [handler (wrap-reactive-consumer process-transaction)]
    (.. @client-atom
        getTransactionsClient
        (getTransactions @ledger-offset-atom filter true)
        (forEach handler))))

(defn start
  [& [{:keys [host port party]
       :or {host "localhost"
            port 6865
            party "USD_Bank"}}]]
  (let [client (DamlLedgerClient/forHostWithLedgerIdDiscovery host port (Optional/empty))
        filter (filter-for {:daml/template-id Credential/TEMPLATE_ID
                            :daml/party party})]
    (reset! client-atom client)
    (.connect @client-atom)
    (populate-credentials-atom filter)
    (start-transaction-listener filter)))

(defn stop
  []
  (.close @client-atom)
  (reset! client-atom nil)
  (reset! ledger-offset-atom nil)
  (reset! credentials-atom nil))

(defn submit!
  [{:keys [id] :as credential}]
  (let [encoded (utils/encode (select-keys credential [:keyfn :pub]))
        cmd (.create (Credential. "USD_Bank" ["EUR_Bank" "Bob"] id encoded))]
    (.. @client-atom
        getCommandSubmissionClient
        (submit (.toString (UUID/randomUUID))
                "CredentialApp"
                (.toString (UUID/randomUUID))
                "USD_Bank"
                Instant/EPOCH
                (.plusSeconds Instant/EPOCH 10)
                (Collections/singletonList cmd))
        (blockingGet))))
