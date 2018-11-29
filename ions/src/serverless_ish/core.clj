(ns serverless-ish.core
  (:require [serverless-ish.schema :as schema]
            [datomic.client.api :as d]))


;; Schema setup
(def get-client
  "This function will return a local implementation of the client
  interface when run on a Datomic compute node. If you want to call
  locally, fill in the correct values in the map."
  (memoize #(d/client {:server-type :ion
                       :region (System/getenv "DATOMIC_CLOUD_AWS_REGION") ;; The Worst Devops(tm)
                       :system (System/getenv "DATOMIC_CLOUD_SYSTEM")           
                       :query-group (System/getenv "DATOMIC_CLOUD_QUERY_GROUP") 
                       :endpoint (System/getenv "DATOMIC_CLOUD_ENDPOINT")
                       :proxy-port (java.lang.Integer/parseInt (System/getenv "DATOMIC_CLOUD_PROXY_PORT"))})))

;; Transact our schema
(comment
  (d/create-database (get-client) {:db-name "chat-db"})
  (def conn (d/connect (get-client) {:db-name "chat-db"}))
  (d/transact conn {:tx-data schema/user})
  (d/transact conn {:tx-data schema/channel})
  (d/transact conn {:tx-data schema/message})
  )


;; Basics

(defn all-channel-names
  "Gets all channel names in the db"
  []
  (let [conn (d/connect (get-client) {:db-name "chat-db"})
        db   (d/db conn)]
    (d/q '[:find ?name
           :where [_ :channel/name ?name]]
         db)))

(defn create-channel!
  "Makes a new channel in the db"
  [channel-name]
  (let [conn (d/connect (get-client) {:db-name "chat-db"})
        db   (d/db conn)]
    (d/transact conn {:tx-data [{:channel/id   (java.util.UUID/randomUUID)
                                 :channel/name channel-name}]})))

(defn all-user-handles
  "Gets all user handles from the db"
  []
  (let [conn (d/connect (get-client) {:db-name "chat-db"})
        db   (d/db conn)]
    (d/q '[:find ?handle
           :where [_ :user/handle ?handle]]
         db)))

(defn user-exists?
  "Returns true if user-email exists in the db"
  [user-email]
  (let [conn (d/connect (get-client) {:db-name "chat-db"})
        db   (d/db conn)
        user (d/q '[:find ?e
                    :in $ ?email
                    :where [?e :user/email ?email]]
                  db user-email)]
    (some? (first user))))

(defn create-user!
  "Makes a new user in the db. Email and handle are required keys"
  [{:keys [email handle profile-img] :as user}]
  (let [conn (d/connect (get-client) {:db-name "chat-db"})
        db   (d/db conn)
        new-user (remove #(nil? (val %)) user)
        tx-data (zipmap (map #(keyword "user" (name %)) (keys new-user))
                        (vals new-user))]
    (condp #(%1 %2) tx-data

      #(not (every? some? [(:user/email %) (:user/handle %)])) {:error "User email and handle are required!"}
      (try (d/transact conn {:tx-data [(assoc tx-data :db/add "user")]})
           (catch Exception e {:error (.getMessage e)})))))

(defn channel-id-by-name
  "Given a channel name, get its UUID"
  [channel-name]
  (let [conn (d/connect (get-client) {:db-name "chat-db"})
        db   (d/db conn)]
    (d/q '[:find ?id
           :in $ ?name
           :where
           [?e :channel/id ?id]
           [?e :channel/name ?name]]
         db channel-name)))

(defn messages-in-channel
  "Gets the messages in a channel"
  [channel-id]
  (let [conn (d/connect (get-client) {:db-name "chat-db"})
        db   (d/db conn)]
    (d/q '[:find ?timestamp ?text
           :in $ ?channel-id
           :where
           [?e :message/channel   ?channel-id]
           [?e :message/timestamp ?timestamp]
           [?e :message/text      ?text]]
         db channel-id)))

(defn add-message-to-channel!
  [sender channel text]
  (let [conn (d/connect (get-client) {:db-name "chat-db"})]
    (d/transact conn {:tx-data [{:message/channel channel
                                 :message/sender  sender
                                 :message/text    text}]})))


(comment
  (all-channel-names)
  (create-channel! "test-channel")
  (channel-id-by-name "test-channel")
  (messages-in-channel (str (first (channel-id-by-name "test-channel"))))
  (all-user-handles)
  (let [conn (d/connect (get-client) {:db-name "chat-db"})
        db   (d/db conn)]
    (-> conn
        (d/transact {:tx-data [[:db/add [:user/email "alice@example.com"]
                                 :user/handle "GoAskAlice"]]})
        :tempids))
  (d/q '[:find ?timestamp ?text
         :in $
         :where
         [?e :message/id _]
         [?e :message/timestamp ?timestamp]
         [?e :message/text      ?text]]
       (d/db conn))
  (create-user! {:email "alice@example.com"
                 :handle "GoAskAlice"})
  (create-user! {:email "bob@example.com"
                 :handle "Hackerman"})
  (create-user! {:email "bob@example.com"
                 :handle "Hackerman"})        ;; => {:error "User bob@example.com already exists!"}
  (create-user! {:email "doris@example.com"}) ;; => {:error "User email and handle are required!"}
  (user-exists? "carol@example.com")
  )
