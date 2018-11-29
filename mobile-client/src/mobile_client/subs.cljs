(ns mobile-client.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-greeting
  (fn [db _]
    (:greeting db)))

(reg-sub
 :get-username
 (fn [db _]
   (:username db)))

(reg-sub
 :get-password
 (fn [db _]
   (:password db)))

(reg-sub
 :get-user
 (fn [db _]
   (:user db)))
