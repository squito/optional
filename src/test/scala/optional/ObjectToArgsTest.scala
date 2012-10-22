package optional

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 *
 */

class ObjectToArgsTest extends FunSuite with ShouldMatchers {

  test("parseStrings") {
    val o = new StringHolder(null, null)
    val parser = new ObjectToArgs(o)
    parser.parse(Array("--name", "hello"))
    o.name should be ("hello")
    parser.parse(Array("--comment", "blah di blah blah"))
    o.name should be ("hello")
    o.comment should be ("blah di blah blah")
    parser.parse(Array("--name", "ooga", "--comment", "stuff"))
    o.name should be ("ooga")
    o.comment should be ("stuff")
  }

  test("parseMixed") {
    val o = new MixedTypes(null, 0)

    val parser = new ObjectToArgs(o)

    parser.parse(Array("--name", "foo", "--count", "17"))
    o.name should be ("foo")
    o.count should be (17)
    parser.parse(Array("--count", "-5"))
    o.name should be ("foo")
    o.count should be (-5)
  }

  test("field parsing") {
    val o = new MixedTypes(null, 0) with FieldParsing

    o.parse(Array("--count", "981", "--name", "wakkawakka"))
    o.name should be ("wakkawakka")
    o.count should be (981)
  }

  test("subclass parsing") {
    val o = new Child(false, null, 0) with FieldParsing

    o.parse(Array("--flag", "true", "--name", "bugaloo"))
    o.name should be ("bugaloo")
    o.flag should be (true)
  }

  test("custom parsers") {
    val o = new SpecialTypes(null, null) with FieldParsing

    o.parse(Array("--name", "blah"))
    o.name should be ("blah")

    evaluating {o.parse(Array("--funky", "xyz"))} should produce [Exception]

    o.parse(Array("--funky", "xyz", "--name", "hi"), preParsers = Iterator(MyFunkyTypeParser))
    o.name should be ("hi")
    o.funky should be (MyFunkyType("xyzoogabooga"))

  }

  test("help message") {
    val o = new StringHolder(null, null)
    val parser = new ObjectToArgs(o)
    val exc1 = evaluating {parser.parse(Array("--xyz", "hello"))} should produce [ArgException]
    //the format is still ugly, but at least there is some info there
    "\\-\\-name\\s.*String".r.findFirstIn(exc1.getMessage()) should be ('defined)
    "\\-\\-comment\\s.*String".r.findFirstIn(exc1.getMessage()) should be ('defined)

    val o2 = new MixedTypes(null, 0)
    val p2 = new ObjectToArgs(o2)
    val exc2 = evaluating {p2.parse(Array("--foo", "bar"))} should produce [ArgException]
    "\\-\\-name\\s.*String".r findFirstIn(exc2.getMessage) should be ('defined)
    "\\-\\-count\\s.*[Ii]nt".r findFirstIn(exc2.getMessage) should be ('defined)  //java or scala types, I'll take either for now

    val exc3 = evaluating {p2.parse(Array("--count", "ooga"))} should produce [ArgException]
    //this message really should be much better.  (a) the number format exception should come first and (b) should indicate that it was while processing the "count" argument
    "\\-\\-name\\s.*String".r findFirstIn(exc3.getMessage) should be ('defined)
    "\\-\\-count\\s.*[Ii]nt".r findFirstIn(exc3.getMessage) should be ('defined)  //java or scala types, I'll take either for now
  }
}


case class StringHolder(val name: String, val comment: String)

case class MixedTypes(val name: String, val count: Int)

//is there an easier way to do this in scala?
class Child(val flag: Boolean, name: String, count: Int) extends MixedTypes(name, count)

case class MyFunkyType(val stuff: String)

object MyFunkyTypeParser extends Parser[MyFunkyType] {
  def canParse(tpe: java.lang.reflect.Type) =
    classOf[MyFunkyType].isAssignableFrom(tpe.asInstanceOf[Class[_]])
  def parse(s: String, tpe: java.lang.reflect.Type) =
    MyFunkyType(s + "oogabooga")
}

case class SpecialTypes(val name: String, val funky: MyFunkyType)