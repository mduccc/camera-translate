package com.indieteam.cameratranslate.process

class Api{
        val api_key = "trnsl.1.1.20180923T193310Z.2f912b8e2ea233a8.91afe2b93af0f6df9f656b85586ebd56f6b53f4a"
    fun url_request(text: String) = "https://translate.yandex.net/api/v1.5/tr.json/translate?text=$text&format=plain&lang=en-vi&key=$api_key"
}