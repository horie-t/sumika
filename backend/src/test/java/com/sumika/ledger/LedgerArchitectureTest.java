package com.sumika.ledger;

import com.sumika.archunit.HexagonalArchitecture;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/**
 * {@code ledger} bounded context がヘキサゴナル構成（依存は常に内向き）であることを検証する。
 *
 * <p>レイヤにクラスが追加されるにつれてルールが効いてくる（#9 以降）。
 */
class LedgerArchitectureTest {

  @Test
  void ledgerContextFollowsHexagonalArchitecture() {
    JavaClasses classes = new ClassFileImporter().importPackages("com.sumika.ledger");

    HexagonalArchitecture.boundedContext("com.sumika.ledger")
        .withDomainLayer("domain")
        .withAdaptersLayer("adapter")
        .incoming("in.web")
        .outgoing("out.persistence")
        .and()
        .withApplicationLayer("application")
        .incomingPorts("port.in")
        .outgoingPorts("port.out")
        .services("service")
        .and()
        .check(classes);
  }
}
