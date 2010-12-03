(ns lifemap.data-test
  (:use
   clojure.test)
  (:require
   [lifemap.data :as data]
   [somnium.congomongo :as cm]))


;; for each test, bind the mongo collection name to :eventstest
;; this means we write to a test collection in our tests
;; see (def m-coll ...) in data.clj
(defn for-each-test [f]
  (binding [data/m-coll :eventstest]
    (f)))


(use-fixtures :each for-each-test)


(deftest should-return-single-event-in-list-when-adding-to-event []
         (let [id (System/currentTimeMillis)
               list-after-adding (data/add {:id id})]
           (is (= 1 (count list-after-adding)) "should only be one event in list")
           (is (= id (:id (first list-after-adding))) "should be same id that we added")))
