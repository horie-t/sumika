package com.sumika.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.List;

/**
 * ヘキサゴナルアーキテクチャ DSL の基底要素。
 *
 * <p>Tom Hombergs『Get Your Hands Dirty on Clean Architecture』(2nd Ed.) の
 * {@code archunit} パッケージを参考にした、依存方向を検証するための流暢 DSL。
 */
abstract class ArchitectureElement {

  final String basePackage;

  ArchitectureElement(String basePackage) {
    this.basePackage = basePackage;
  }

  String fullQualifiedPackage(String relativePackage) {
    return this.basePackage + "." + relativePackage;
  }

  /** {@code fromPackage} のクラスが {@code toPackage} に依存していないことを検証する。 */
  static void denyDependency(String fromPackageName, String toPackageName, JavaClasses classes) {
    noClasses()
        .that()
        .resideInAPackage(matchAllClassesInPackage(fromPackageName))
        .should()
        .dependOnClassesThat()
        .resideInAPackage(matchAllClassesInPackage(toPackageName))
        // レイヤがまだ空でも（スケルトン段階でも）失敗させない
        .allowEmptyShould(true)
        .check(classes);
  }

  static void denyAnyDependency(
      List<String> fromPackages, List<String> toPackages, JavaClasses classes) {
    for (String fromPackage : fromPackages) {
      for (String toPackage : toPackages) {
        denyDependency(fromPackage, toPackage, classes);
      }
    }
  }

  static String matchAllClassesInPackage(String packageName) {
    return packageName + "..";
  }

  /** 各パッケージが（package-info を除く）実クラスを 1 つ以上含むことを検証する。 */
  static void denyEmptyPackages(List<String> packages, JavaClasses classes) {
    for (String packageName : packages) {
      if (!containsRealClass(classes, packageName)) {
        throw new AssertionError(
            "Hexagonal architecture violated: package is empty: " + packageName);
      }
    }
  }

  private static boolean containsRealClass(JavaClasses classes, String packageName) {
    for (JavaClass clazz : classes) {
      String pkg = clazz.getPackageName();
      boolean inPackage = pkg.equals(packageName) || pkg.startsWith(packageName + ".");
      if (inPackage && !clazz.getName().endsWith(".package-info")) {
        return true;
      }
    }
    return false;
  }
}
