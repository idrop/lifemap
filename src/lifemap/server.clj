(ns lifemap.server
  (:require
   [lifemap.utils :as utils]
   [lifemap.data :as data]
   [compojure.response :as resp]
   [compojure.route :as route])
  (:use
   compojure.core
   ring.adapter.jetty))

(comment
  an event looks like this when it comes in as params
  { "lng" "-0.13677564177418056" "lat" "51.311495428201916" "fbid" "642791293"
    "when" "62985600000" "desc" "description" "tag" "some tag"})

(defn- respond
  "given a list of events, sanitizes each event, jsonizes it, then renders reponse"
  [coll request]
  (->>
   (utils/clean-response coll)
   (utils/to-json ,,,)
   (resp/render request ,,,)))


;; ring handler function in which route functions are defined
(defroutes main-routes


  (POST "/add" {params :params request :request}
        (let [event ((comp utils/translate utils/keywordize) params)]
          (if (utils/valid? event)
            (->
             (dissoc event :id)
             (data/add ,,,)
             (respond ,,, request)))))

  
  (POST "/update" {params :params request :request}
        (let [event ((comp utils/translate utils/keywordize) params)]
          (if (utils/valid? event)
            (respond (data/update event) request))))

  
  (POST "/list" [fbid {request :request}] 
        (->
         (utils/string-to-long fbid)
         (data/all ,,,)
         (respond ,,, request)))

  
  (POST "/delete" [id {request :request}]
        (respond (data/delete id) request))

  
  ;; given GET http://localhost:8888/ping?foo=test, returns "pong test"
  (GET "/ping" [foo]
       (str "pong " foo))

  
  ;; all other content is served out of public/  
  (route/files "/" {:root "public"})


  ;; why doesn't this work?
  (route/not-found "Not found."))


(defn start
  "start jetty server"
  []
  (run-jetty #'main-routes {:port 8888 :join? false}))


