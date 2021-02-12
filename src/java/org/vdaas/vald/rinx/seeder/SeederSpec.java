package org.vdaas.vald.rinx.seeder;

public class SeederSpec {

  private String host;
  private int port;

  private String edn;

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

  public String getEdn() {
    return edn;
  }

  public void setEdn(String edn) {
    this.edn = edn;
  }
}
