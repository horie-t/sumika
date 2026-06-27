package com.sumika.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.List;

/** アダプタ層（incoming / outgoing）の構成と依存ルールを表す DSL 要素。 */
public class Adapters extends ArchitectureElement {

  private final HexagonalArchitecture parent;
  private final List<String> incomingAdapterPackages = new ArrayList<>();
  private final List<String> outgoingAdapterPackages = new ArrayList<>();

  Adapters(HexagonalArchitecture parent, String basePackage) {
    super(basePackage);
    this.parent = parent;
  }

  public Adapters incoming(String packageName) {
    this.incomingAdapterPackages.add(fullQualifiedPackage(packageName));
    return this;
  }

  public Adapters outgoing(String packageName) {
    this.outgoingAdapterPackages.add(fullQualifiedPackage(packageName));
    return this;
  }

  public HexagonalArchitecture and() {
    return this.parent;
  }

  String getBasePackage() {
    return this.basePackage;
  }

  private List<String> allAdapterPackages() {
    List<String> all = new ArrayList<>();
    all.addAll(this.incomingAdapterPackages);
    all.addAll(this.outgoingAdapterPackages);
    return all;
  }

  /** incoming / outgoing アダプタが互いに直接依存しないことを検証する。 */
  void dontDependOnEachOther(JavaClasses classes) {
    List<String> all = allAdapterPackages();
    for (String adapter1 : all) {
      for (String adapter2 : all) {
        if (!adapter1.equals(adapter2)) {
          denyDependency(adapter1, adapter2, classes);
        }
      }
    }
  }

  void doesNotDependOn(String packageName, JavaClasses classes) {
    denyDependency(this.basePackage, packageName, classes);
  }
}
