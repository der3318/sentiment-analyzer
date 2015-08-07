# SentimentAnalyzer-正反情緒分析

## 開發環境
* Eclipse
* JDK/JRE ver.1.8
* 中文編碼utf-8

## 架構
* 外部字典(正面、反面、程度詞)
* Training Data -> 多找出一些此類文章的正反面情緒詞
* Testing -> 利用上面2個steps建立的字典，predict結果

## 字典儲存
* HashMap (Key, Value) = (情緒詞, 正/反)
* 可以在O(1)的時間內判斷單詞是否具有特定情緒

## 如何分出單詞
* Step1: 用標點、換行斷句
* Step2: 利用MMseg(OpenSource)，實現最大匹配、最大單詞長度去斷詞

## Training方式
* Step1: 斷詞
* Step2: 統計各單詞出現在正評、反評的頻率
* Step3: 可利用SO-PMI的方法，將單詞加入情緒字典

## Predict方式
* Step1: 斷詞
* Step2: 先過濾副詞，再利用字典判斷剩餘詞彙是否為情緒詞
* Step3: 根據句子的情緒詞與副詞，得到一個Score
* Step4: 加總各句子的Score，得到整個評論的TotalScore，用來predict正反情緒