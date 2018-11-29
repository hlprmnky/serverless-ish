(ns serverless-ish.ions
  (:require [serverless-ish.core :as core]))

(defn get-channels
  "Returns all the extant channel names"
  [{:keys [input]}]
  (core/all-channel-names))

(defn get-users
  "Returns all known user handles"
  [{:keys [input]}]
  (core/all-user-handles))

(defn create-channel
  "Creates a new channel"
  [{:keys [input]}]
  (core/create-channel! (:channel-name input)))

(defn send-message!
  "Sens a message to a channel"
  [{:keys [input]}]
  (core/add-message-to-channel! (:channel-name input)
                                (:user-email input)
                                (:message-body input)))


(comment
  (get-users {:input ""})
  (core/all-user-handles)
  (get-channels {:input ""})
  )
