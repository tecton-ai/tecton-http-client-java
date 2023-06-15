package ai.tecton.client.model;

import ai.tecton.client.exceptions.TectonClientException;
import ai.tecton.client.exceptions.TectonErrorMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
class ListDataType {

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
        if (featureObjectList != null) {
          this.int64List =
              featureObjectList.stream()
                  .map(
                      obj -> {
                        String stringValue = (String) obj;
                        return (stringValue != null) ? Long.parseLong(stringValue) : null;
                      })
                  .collect(Collectors.toList());
        } else {
          this.int64List = null;
        }
        break;
      case FLOAT32:
        if (featureObjectList != null) {
          this.float32List = new ArrayList<>(featureObjectList.size());
          featureObjectList.forEach(obj -> this.float32List.add((Float) obj));
        } else {
          this.float32List = null;
        }
        break;
      case FLOAT64:
        if (featureObjectList != null) {
          this.float64List = new ArrayList<>(featureObjectList.size());
          featureObjectList.forEach(obj -> this.float64List.add((Double) obj));
        } else {
          this.float64List = null;
        }
        break;
      case STRING:
        if (featureObjectList != null) {
          this.stringList = new ArrayList<>(featureObjectList.size());
          featureObjectList.forEach(obj -> this.stringList.add((String) obj));
        } else {
          this.stringList = null;
        }
        break;
      default:
        throw new TectonClientException(TectonErrorMessage.UNSUPPORTED_LIST_DATA_TYPE);
    }
  }

  /** Overrides <i>equals()</i> in class {@link Object} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ListDataType that = (ListDataType) o;
    return Objects.equals(stringList, that.stringList)
        && Objects.equals(float32List, that.float32List)
        && Objects.equals(float64List, that.float64List)
        && Objects.equals(int64List, that.int64List)
        && Objects.equals(listElementType, that.listElementType);
  }

  /** Overrides <i>hashCode()</i> in class {@link Object} */
  @Override
  public int hashCode() {
    return Objects.hash(stringList, float32List, float64List, int64List, listElementType);
  }
}
