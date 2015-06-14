import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.{FileTime, BasicFileAttributeView}
import java.text.SimpleDateFormat
import java.util.Date

import com.rocktto.refine.Refine.{size => _size, _}
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by leonardootto on 29/05/15.
 */
class RefineTest extends FlatSpec with Matchers {

  //Utility methods

  def modifiyFileToModifiedDate(file: File, date: Date) = {
    val attributes = Files.getFileAttributeView(file.toPath, classOf[BasicFileAttributeView])
    val time = FileTime.fromMillis(date.getTime)
    attributes.setTimes(time, time, time)
  }

  val resDirectory = new File(getClass.getResource("./").toURI)

  "A Bird" should "exists" in {
    try {
      println(resDirectory)
      Class.forName("com.rocktto.refine.Refine")
    } catch {
      case ex: Exception => fail("Class not exists!")
    }
  }

  it should "list files in directory" in {
    val testDir = new File(resDirectory, "list_01")

    val fileList = files(testDir)
    assert(fileList.size == 3)
  }

  it should "list files in directory when implicit" in {
    implicit val testDir = new File(resDirectory, "list_01")

    val fileList = files
    assert(fileList.size == 3)
  }

  it should "filter file list with name" in {
    implicit val testDir = new File(resDirectory, "list_01")

    var list = filter(files, name("file"))
    assert(list.size == 3)

    list = filter(files, name("file1"))
    assert(list.size == 1)
  }

  it should "filter file list with size is exact X bytes" in {
    implicit val testDir = new File(resDirectory, "list_01")

    val list = filter(files, name("file"), _size(5)) // i rename size to _size to not conflict with matcher
    assert(list.size == 2)
  }

  it should "filter file list with size is exact X kbytes" in {
    implicit val testDir = new File(resDirectory, "list_03")

    val list = filter(files, name("file"), _size(kb(1))) // exactly 1024 bytes
    assert(list.size == 1)
  }

  it should "filter file list with size is more that 1 bytes" in {
    implicit val testDir = new File(resDirectory, "list_01")

    val list = filter(files, name("file"), _size(bigger(1))) // bigger that 1 byte
    assert(list.size == 2)
  }

  it should "filter file list with size is less that 2 bytes" in {
    implicit val testDir = new File(resDirectory, "list_01")

    val list = filter(files, name("file"), _size(lower(2))) // bigger that 1 byte
    assert(list.size == 1)
  }

  it should "filter file list with size is between 1 byte and "

  it should "find file names using regular expression" in {
    implicit val testDir = new File(resDirectory, "list_04")

    val list = filter(files, name("file.\\.txt".r))
    assert(list.size == 5)
  }

  it should "find file by creation date" in {
    implicit val testDir = new File(resDirectory, "list_04")
    val sdf = new SimpleDateFormat("MM/dd/yyyy")

    //we need force modification date, because the resources files is copied every time
    //to output folder and the modification date is overwrite by build tool
    modifiyFileToModifiedDate(new File(testDir, "file7.txt"), sdf.parse("05/29/2015"))

    val list = filter(files, date("05/29/2015"))
    assert(list.size == 1)
  }

  it should "find files by creation date lower that today" in {
    implicit val testDir = new File(resDirectory, "list_04")
    val sdf = new SimpleDateFormat("MM/dd/yyyy")

    //modify all file dates to today
    files.foreach(modifiyFileToModifiedDate(_,today()))

    //change 1 date to lower
    modifiyFileToModifiedDate(new File(testDir, "file7.txt"), sdf.parse("05/29/2000"))

    val list = filter(files, date(lower(today())))
    assert(list.size == 1)
  }

  it should "find all files in directory" in {
    implicit val testDir = new File(resDirectory, "list_05")

    val allFiles = recursive(files)
    assert(allFiles.size == 15)
  }

}
