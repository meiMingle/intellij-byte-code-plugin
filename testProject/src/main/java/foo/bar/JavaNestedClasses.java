// FileTestVector{baseFqClassNames: foo.bar.JavaNestedClasses, containingFqClassNames: foo.bar.JavaNestedClasses|foo.bar.JavaNestedClasses$Nested|foo.bar.JavaNestedClasses$Inner|foo.bar.JavaNestedClasses$Inner$Record }
package foo.bar;

public class JavaNestedClasses {

  void method() {
    // PsiElementTestVector{reference: METHOD|JavaNestedClasses#method, baseFqClassName: foo.bar.JavaNestedClasses, expectedFqClassNames: foo.bar.JavaNestedClasses}
  }

  void methodWithLocalClass() {
    new Thread() {
      @Override
      public void run() {
        // PsiElementTestVector{reference: METHOD|JavaNestedClasses#methodWithLocalClass, baseFqClassName: foo.bar.JavaNestedClasses, expectedFqClassNames: foo.bar.JavaNestedClasses}
      }
    };
  }

  public interface Nested {

    private void method() {
      // PsiElementTestVector{reference: METHOD|JavaNestedClasses$Nested#method, baseFqClassName: foo.bar.JavaNestedClasses, expectedFqClassNames: foo.bar.JavaNestedClasses$Nested}
    }
  }

  public class Inner {

    void method() {
      // PsiElementTestVector{reference: METHOD|JavaNestedClasses$Inner#method, baseFqClassName: foo.bar.JavaNestedClasses, expectedFqClassNames: foo.bar.JavaNestedClasses$Inner}
    }

    public record Record(String parameter) {

      void method() {
        // PsiElementTestVector{reference: METHOD|JavaNestedClasses$Inner$Record#method, baseFqClassName: foo.bar.JavaNestedClasses, expectedFqClassNames: foo.bar.JavaNestedClasses$Inner$Record}
      }
    }
  }
}