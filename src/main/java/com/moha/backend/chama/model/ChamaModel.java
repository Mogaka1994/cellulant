package com.moha.backend.chama.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"name",
"chama_id"
})
public class ChamaModel {

@JsonProperty("name")
private String name;
@JsonProperty("chama_id")
private String chamaId;

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("chama_id")
public String getChamaId() {
return chamaId;
}

@JsonProperty("chama_id")
public void setChamaId(String chamaId) {
this.chamaId = chamaId;
}

}