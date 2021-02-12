package org.vdaas.vald.rinx.seeder;

import java.util.List;

public class SeederStatus {

  private String host;
  private int port;

  private String status;
  private String error;

  private List<String> ids;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
      this.error = error;
  }

  public List<String> getIds() {
      return ids;
  }

  public void setIds(List<String> ids) {
      this.ids = ids;
  }
}
