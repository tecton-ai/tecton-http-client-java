package com.tecton.client.model;

import com.tecton.client.exceptions.TectonClientException;
import com.tecton.client.exceptions.TectonErrorMessage;

import java.util.List;

public class ListDataType {
  List<String> stringList;
  List<Float> float32List;
  List<Double> float64List;
  List<Long> int64List;
  ValueType listElementType;

  ListDataType(ValueType listElementType, List<Object> featureObjectList) {
    this.listElementType = listElementType;
    switch (listElementType) {
      case INT64:
        featureObjectList.forEach(obj -> this.int64List.add((Long) obj));
        break;
      case FLOAT32:
        featureObjectList.forEach(obj -> this.float32List.add((Float) obj));
        break;
      case FLOAT64:
        featureObjectList.forEach(obj -> this.float64List.add((Double) obj));
        break;
      case STRING:
        featureObjectList.forEach(obj -> this.stringList.add((String) obj));
        break;
      default:
        throw new TectonClientException(TectonErrorMessage.UNSUPPORTED_LIST_DATA_TYPE);
    }
  }
}
