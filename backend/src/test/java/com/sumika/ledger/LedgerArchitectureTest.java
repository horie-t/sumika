package com.sumika.ledger;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.sumika.archunit.HexagonalArchitecture;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

/** {@code ledger} bounded context のアーキテクチャ検証（依存方向・層構成・非循環）。 */
class LedgerArchitectureTest {

  private final JavaClasses ledgerClasses =
      new ClassFileImporter().importPackages("com.sumika.ledger");

  @Test
  void ledgerContextFollowsHexagonalArchitecture() {
    HexagonalArchitecture.boundedContext("com.sumika.ledger")
        .withDomainLayer("domain")
        .withAdaptersLayer("adapter")
        .incoming("in.web")
        .outgoing("out.persistence")
        .outgoing("out.security")
        .and()
        .withApplicationLayer("application")
        .incomingPorts("port.in")
        .outgoingPorts("port.out")
        .services("service")
        .and()
        .check(this.ledgerClasses);
  }

  @Test
  void domainIsFrameworkIndependent() {
    noClasses()
        .that()
        .resideInAPackage("com.sumika.ledger.domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.hibernate..")
        .check(this.ledgerClasses);
  }

  @Test
  void ledgerSlicesAreFreeOfCycles() {
    slices()
        .matching("com.sumika.ledger.(*)..")
        .should()
        .beFreeOfCycles()
        .check(this.ledgerClasses);
  }
}
