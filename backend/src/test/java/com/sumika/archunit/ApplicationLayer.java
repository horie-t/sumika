package com.sumika.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.List;

/** アプリケーション層（incoming/outgoing ポート、サービス）の構成と依存ルールを表す DSL 要素。 */
public class ApplicationLayer extends ArchitectureElement {

  private final HexagonalArchitecture parent;
  private final List<String> incomingPortsPackages = new ArrayList<>();
  private final List<String> outgoingPortsPackages = new ArrayList<>();
  // サービスパッケージ。空パッケージ検証(#14)で使用する。
  private final List<String> servicePackages = new ArrayList<>();

  ApplicationLayer(String basePackage, HexagonalArchitecture parent) {
    super(basePackage);
    this.parent = parent;
  }

  public ApplicationLayer incomingPorts(String packageName) {
    this.incomingPortsPackages.add(fullQualifiedPackage(packageName));
    return this;
  }

  public ApplicationLayer outgoingPorts(String packageName) {
    this.outgoingPortsPackages.add(fullQualifiedPackage(packageName));
    return this;
  }

  public ApplicationLayer services(String packageName) {
    this.servicePackages.add(fullQualifiedPackage(packageName));
    return this;
  }

  public HexagonalArchitecture and() {
    return this.parent;
  }

  String getBasePackage() {
    return this.basePackage;
  }

  /** 空でないことを検証する対象パッケージ（in/out ポート・サービス）。 */
  List<String> leafPackages() {
    List<String> all = new ArrayList<>();
    all.addAll(this.incomingPortsPackages);
    all.addAll(this.outgoingPortsPackages);
    all.addAll(this.servicePackages);
    return all;
  }

  void doesNotDependOn(String packageName, JavaClasses classes) {
    denyDependency(this.basePackage, packageName, classes);
  }

  /** incoming ポートと outgoing ポートが互いに依存しないことを検証する。 */
  void incomingAndOutgoingPortsDoNotDependOnEachOther(JavaClasses classes) {
    denyAnyDependency(this.incomingPortsPackages, this.outgoingPortsPackages, classes);
    denyAnyDependency(this.outgoingPortsPackages, this.incomingPortsPackages, classes);
  }
}
