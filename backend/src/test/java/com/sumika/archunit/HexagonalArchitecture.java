package com.sumika.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.List;

/**
 * bounded context をヘキサゴナル（ポート&アダプタ）構成として検証する DSL のエントリポイント。
 *
 * <pre>{@code
 * HexagonalArchitecture.boundedContext("com.sumika.ledger")
 *     .withDomainLayer("domain")
 *     .withAdaptersLayer("adapter")
 *         .incoming("in.web")
 *         .outgoing("out.persistence")
 *         .and()
 *     .withApplicationLayer("application")
 *         .incomingPorts("port.in")
 *         .outgoingPorts("port.out")
 *         .services("service")
 *         .and()
 *     .check(classes);
 * }</pre>
 */
public class HexagonalArchitecture extends ArchitectureElement {

  private Adapters adapters;
  private ApplicationLayer applicationLayer;
  private final List<String> domainPackages = new ArrayList<>();

  public static HexagonalArchitecture boundedContext(String basePackage) {
    return new HexagonalArchitecture(basePackage);
  }

  private HexagonalArchitecture(String basePackage) {
    super(basePackage);
  }

  public HexagonalArchitecture withDomainLayer(String domainPackage) {
    this.domainPackages.add(fullQualifiedPackage(domainPackage));
    return this;
  }

  public Adapters withAdaptersLayer(String adaptersPackage) {
    this.adapters = new Adapters(this, fullQualifiedPackage(adaptersPackage));
    return this.adapters;
  }

  public ApplicationLayer withApplicationLayer(String applicationPackage) {
    this.applicationLayer = new ApplicationLayer(fullQualifiedPackage(applicationPackage), this);
    return this.applicationLayer;
  }

  private void domainDoesNotDependOnOtherPackages(JavaClasses classes) {
    denyAnyDependency(this.domainPackages, List.of(this.adapters.getBasePackage()), classes);
    denyAnyDependency(this.domainPackages, List.of(this.applicationLayer.getBasePackage()), classes);
  }

  /**
   * 依存方向（常に内向き: {@code adapter -> application -> domain}）を検証する。
   *
   * <p>各レイヤが空でないことの検証（{@code doesNotContainEmptyPackages}）は、
   * 各層にクラスが揃う #14 で有効化する。
   */
  public void check(JavaClasses classes) {
    this.adapters.dontDependOnEachOther(classes);
    this.applicationLayer.doesNotDependOn(this.adapters.getBasePackage(), classes);
    this.applicationLayer.incomingAndOutgoingPortsDoNotDependOnEachOther(classes);
    this.domainDoesNotDependOnOtherPackages(classes);
  }
}
