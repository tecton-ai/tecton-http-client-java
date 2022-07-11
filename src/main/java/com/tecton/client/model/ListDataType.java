package com.tecton.client.model;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ListDataType {
  List<String> stringList;
  List<Float> float32List;
  List<Double> float64List;
  List<Long> int64List;
  ValueType listElementType;

  ListDataType(ValueType listElementType, Object featureObject) {
    List<Object> featureObjectList = (ArrayList<Object>) featureObject;
    this.listElementType = listElementType;
    // Parse List of Object to List of corresponding Java type
    switch (listElementType) {
      case INT64:
        this.int64List = new ArrayList<>(featureObjectList.size());
        featureObjectList.forEach(obj -> this.int64List.add(Long.parseLong((String) obj)));
        break;
      case FLOAT32:
        this.float32List = new ArrayList<>(featureObjectList.size());
        featureObjectList.forEach(obj -> this.float32List.add((Float) obj));
        break;
      case FLOAT64:
        this.float64List = new ArrayList<>(featureObjectList.size());
        featureObjectList.forEach(obj -> this.float64List.add((Double) obj));
        break;
      case STRING:
        this.stringList = new ArrayList<>(featureObjectList.size());
        featureObjectList.forEach(obj -> this.stringList.add((String) obj));
        break;
      default:
        throw new TectonClientException(TectonErrorMessage.UNSUPPORTED_LIST_DATA_TYPE);
    }
  }
}
