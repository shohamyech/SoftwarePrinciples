(ns clo-proj.core
  (:require [clojure.string :as str])
  (:require [clojure.java.io :as io])
  (:gen-class))

(println "Enter path")
(def path (read-line))

;checking folder exists
(when-not (.exists (java.io.File. path))
  (println "Invalid path")
  (System/exit 0))

;constructing the output file name - the same as the folder
(def output-file 
  (str path "\\" (re-find #"\w*$" path) ".asm"))

(def total-buy  0)
(def total-sell 0)

;recieves ["buy" "bread" "3" "4.5"] (example)
;outputs "###BUY bread ###\n13.5"
(defn HandleBuy [s]
  (let [cost (* (Double/parseDouble (get s 2))
                (Double/parseDouble (get s 3)))]
    (alter-var-root #'total-buy (constantly (+ total-buy cost)))

    (str "### BUY " (get s 1) " ###\n"
         (format "%,.2f" cost) "\n")))

;same as HandleBuy
(defn HandleSell [s]
  (let [cost (* (Double/parseDouble (get s 2))
                (Double/parseDouble (get s 3)))]
    (alter-var-root #'total-sell (constantly (+ total-sell cost)))

    (str "$$$ SELL " (get s 1) " $$$\n"
         (format "%,.2f" cost) "\n"))
)

;recieves ["buy\sell" "bread" "3" "4.5"] (example)
;if first value is buy calls HandleBuy else calls HandleSell sell
(defn buy-or-sell [s]
  (if (= (first s) "buy")
    (HandleBuy s)
    (HandleSell s)))

;recieves ["InputA.vm" "buy bread 3 4.5\nbuy cheese 2 5.3\n..."]
;outputs "### BUY bread ###\n13.5\n### BUY cheese ###\n10.6\n..."
(defn HandleFile [file]
  (apply str (first file) "\n"
         (->> (last file)
              (str/split-lines)
              (map #(str/split % #" "))
              (map buy-or-sell))))

;gets vm files names from folder
(def files-names 
(->> (io/file path)
     (file-seq)
     (filter #(.isFile %))
     (map str)
     (map #(re-find #"\w*.vm$" %))
     (remove nil?)
     (map #(str/replace % #".vm$" ""))))

(->> (io/file path)                      ;get folder file obj
     (file-seq)                          ;get all files from folder
     (filter #(.isFile %))               ;filter files
     (map str)                           ;map to files path as string
     (filter #(re-find #".vm$" %))       ;filter vm files
     (map slurp)                         ;map to the files content
     (map vector files-names)            ;add file name to each file content
     (map HandleFile)                    ;convert to HandleFile result
     (str/join)                          ;join all files
     (spit output-file))                 ;write to output file

;writing total buy and sell to output file
(spit output-file
      (str "\nTOTAL BUY: " (format "%,.2f" total-buy)
           "\nTOTAL SELL: " (format "%,.2f" total-sell)) :append true)


