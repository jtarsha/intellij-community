class Test {

  // IDEA-182933
  public static void main(String[] args) {
    int a = 1;
    int b = 2;
    boolean f = false;

    String s = "Hello " + (a + b) + (<warning descr="Condition 'f' is always 'false'"><caret>f</warning> ? " Earth" : " World");

    System.out.println(s); // Prints Hello 3 World
  }
}