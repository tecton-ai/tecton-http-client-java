package com.tecton.client.model;

import java.util.HashMap;
import java.util.Map;

public class GetFeaturesRequestData {

  private Map<String, Object> joinKeyMap;
  private Map<String, Object> requestContextMap;

  public GetFeaturesRequestData() {
    this.joinKeyMap = new HashMap<>();
    this.requestContextMap = new HashMap<>();
  }
}
