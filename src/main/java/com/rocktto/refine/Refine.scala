package com.rocktto.refine

import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.scalalogging.Logger
import org.joda.time.LocalDate
import org.slf4j.LoggerFactory

import scala.util.matching.Regex

/**
 * Created by leonardootto on 29/05/15.
 */
class Refine {

}

object Refine {
  val log = Logger(LoggerFactory.getLogger(classOf[Refine]))

  def recursive(list: List[File]): List[File] = {
    val files = list.map(file=>
      if(file.isDirectory) {
        val files = recursive(file.listFiles.toList)
        file +: files
      }else{
        List(file)
      }
    )
    files.flatMap(f=>f)
  }

  def files(implicit directory: File): List[File] = {
    val listFiles = directory.listFiles().toList

    log.debug(s"${listFiles.size} files find in directory ${directory.getName}")

    listFiles
  }

  def filter(files: List[File], filters: Filter*): List[File] = {
    val filteredFiles =
      files.filter(file => {
        filters.forall(_.applicable(file))
      })

    log.debug(s"${files.foreach(_.getName)} files find for filters: $filters")

    filteredFiles
  }

  def name(name: String): Filter = {
    NameFilter(name)
  }

  def name(regex: Regex): Filter = {
    RegexFilter(regex)
  }

  def date(strDate: String): Filter = {
    ExactDateFilter(strDate)
  }

  def date(dateFilter: DateFilter): Filter = {
    dateFilter
  }

  def today(): Date = new Date()

  def size(sizeInBytes: Long): ExactSizeFilter = {
    ExactSizeFilter(sizeInBytes)
  }

  def size(range: RangeSizeFilter): Filter = {
    range
  }

  def kb(sizeKBytes: Long): Long = {
    sizeKBytes * 1024
  }

  def bigger(sizeBytes: Long): RangeSizeFilter = {
    RangeSizeFilter(sizeBytes, Long.MaxValue)
  }

  def lower(sizeBytes: Long): RangeSizeFilter = {
    RangeSizeFilter(0, sizeBytes)
  }

  def bigger(date: Date): RangeDateFilter = {
    RangeDateFilter(date, new Date(Long.MaxValue))
  }

  def lower(date: Date): RangeDateFilter = {
    RangeDateFilter(new Date(0), date)
  }
}

trait Filter {
  def applicable(file: File): Boolean
}

case class NameFilter(name: String) extends Filter {
  override def applicable(file: File): Boolean = {
    file.getName.contains(name)
  }
}

case class RegexFilter(regex: Regex) extends Filter {
  override def applicable(file: File): Boolean = {
    regex.pattern.matcher(file.getName).matches()
  }
}

trait DateFilter extends Filter {
  def getLastModifiedDate(file: File): Date = {
    val attributes = Files.readAttributes(file.toPath, classOf[BasicFileAttributes])
    val date = new Date(attributes.lastModifiedTime().toMillis)
    date
  }

  def getCreateFileDate(file: File): Date = {
    val attributes = Files.readAttributes(file.toPath, classOf[BasicFileAttributes])
    val date = new Date(attributes.creationTime().toMillis)
    date
  }

  def getLastAccessTime(file: File): Date = {
    val attributes = Files.readAttributes(file.toPath, classOf[BasicFileAttributes])
    val date = new Date(attributes.lastAccessTime().toMillis)
    date
  }
}

case class ExactDateFilter(strDate: String) extends DateFilter {
  val formatter = new SimpleDateFormat("MM/dd/yyyy")

  override def applicable(file: File): Boolean = {
    println(s"File ${file.getName} creation date is ${getCreateFileDate(file)}")
    strDate.equals(formatter.format(getCreateFileDate(file)))
  }
}

case class RangeDateFilter(lowerDate: Date, biggerDate: Date) extends DateFilter {
  override def applicable(file: File): Boolean = {
    val date = new LocalDate(getCreateFileDate(file))

    val ld = new LocalDate(lowerDate)
    val bd = new LocalDate(biggerDate)

    date.compareTo(ld) > 0 && date.compareTo(bd) < 0
  }
}

trait SizeFilter extends Filter

case class ExactSizeFilter(sizeInBytes: Long) extends SizeFilter {
  override def applicable(file: File): Boolean = {
    file.length == sizeInBytes
  }
}

case class RangeSizeFilter(lowerSize: Long, biggerSize: Long) extends SizeFilter {
  override def applicable(file: File): Boolean = {
    file.length >= lowerSize && file.length <= biggerSize
  }
}



