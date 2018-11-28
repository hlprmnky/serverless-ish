(ns serverless-ish.schema
  (:require [datomic.client.api :as d]))

(def user
  [{:db/ident :user/id
    :db/valueType  :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :user/email
    :db/valueType :db.type/string
    :db/unique :db.unique/value
    :db/cardinality :db.cardinality/one
    :db/doc "Unique email, used for account management and administrivia"}
   {:db/ident :user/handle
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "'handle' is a display-name field"}
   {:db/ident :user/profile-img
    :db/valueType :db.type/uri
    :db/cardinality :db.cardinality/one
    :db/doc "User avatar image"}])

(def channel
  [{:db/ident :channel/id
    :db/valueType :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :channel/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Channel ID for humans"}
   {:db/ident :channel/users
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many}])

(def message
  [{:db/ident :message/id
    :db/valueType :db.type/uuid
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :message/sender
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :message/channel
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "Note that even 'private messages' are sent
             to a channel (with only two users),
             not to a user"}
   {:db/ident :message/text
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Just simple strings for this simple client"}
   {:db/ident :message/timestamp
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one}])

