(ns clj-boost-demo.core
  (:require [clj-boost.core :as boost]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]))

(def iris-path "resources/iris.csv")

(defn -main []
  (println "Welcome to clj-xgboost-demo!")
  (let [split-set (->> iris-path
                       generate-iris
                       munge-data
                       (train-test-split 120))
        [train test] (map #(% split-set) [train-set test-set])
        model (train-model train)
        result (predict-model model test)]
    (println "Prediction: " (mapv int result))
    (println "Real: " (:y test))
    (println "Accuracy: " (accuracy result (:y test)))))

(defn generate-iris
  [iris-path]
  (with-open [reader (io/reader iris-path)]
    (into []
          (comp (drop 1) (map #(split-at 4 %)))
          (csv/read-csv reader))))

(defn parse-float [s]
  (Float/parseFloat s))

(->> (generate-iris iris-path)
     (take 2)
     (map first)) ; thread-macro is super awesome!!!

(->> (generate-iris iris-path)
     (take 2)
     (map first)
     (map #(map parse-float %)))

(->> (generate-iris iris-path)
     (take 2)
     (map first)
     (map #(map parse-float %))
     (map vec))

(def transform-x
  (comp
   (map first)
   (map #(map parse-float %))
   (map vec)))

(def transform-y
  (comp
   (map last)
   (map (fn [label]
          (let [l (first label)]
                case l
                "setosa" 0
                "versicolor" 1
                "virginica" 2)))))

(defn munge-data [iris-data]
  (let [x (into [] transform-x iris-data)
        y (into [] transform-y iris-data)]
    (map conj x y))) ; Concatenates both lists

(defn train-test-split [n dataset]
  (let [shuffled (shuffle dataset)]
    (split-at n shuffled))) ; Takes a collection and number of instances and shuffles them

(defn train-set [split-set]
  (let [set (first split-set)]
    {:x (mapv drop-last set)
     :y (mapv last set)}))

(defn test-set [split-set]
  (let [set (last split-set)]
    {:x (mapv drop-last set)
     :y (mapv last set)}))

(defn train-model [train-set]
  (let [data (boost/dmatrix train-set)
        params {:params {:eta 0.00001 ;; This is the learning rate
                         :objective "multi:softmax"
                         :num_class 3} ;; Tells xgboost how many classes we have
                :rounds 2 ;; This defines how many rounds of boosting we do
                :watches {:train-data}
                :early-stopping 10}]
    (boost/fit data params))) ;; Trains an xgboost model from scratch and returns a boost instance

(defn predict-model [model test-set]
  (boost/predict model (boost/dmatrix test-set)))

(defn accuracy [predicted real]
  (let [right (map #(compare %1 %2) predicted real)]
    (/ (count (filter zero? right))
       (count real))))
