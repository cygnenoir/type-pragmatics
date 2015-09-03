package de.tu_darmstadt.veritas.backend.veritas

import de.tu_darmstadt.veritas.backend.nameresolution.nabl.VeritasModuleUri
import de.tu_darmstadt.veritas.backend.stratego.StrategoAppl
import de.tu_darmstadt.veritas.backend.stratego.StrategoTerm
import de.tu_darmstadt.veritas.backend.stratego.StrategoInt
import de.tu_darmstadt.veritas.backend.stratego.StrategoString
import de.tu_darmstadt.veritas.backend.util.prettyprint.PrettyPrintable
import de.tu_darmstadt.veritas.backend.util.prettyprint.PrettyPrintWriter
import de.tu_darmstadt.veritas.backend.util.prettyprint.SimplePrettyPrintable
import de.tu_darmstadt.veritas.backend.util.Context
import de.tu_darmstadt.veritas.backend.util.BackendError
import de.tu_darmstadt.veritas.backend.nameresolution.NameResolution
import de.tu_darmstadt.veritas.backend.stratego.StrategoList

/**
 * Imports of other Veritas modules. 
 * Spoofax does imports by annotating the import AST node with a "NaBL Use". These Uses can have
 * different forms, i.e. a "Task ID" (such as "Use(Result(<someInteger>))") or a Module URI.  
 */

sealed trait Import extends SimplePrettyPrintable {
  private var _importAnnotations: Seq[ImportAnnotation] = Seq()
  def importAnnotations = _importAnnotations
  def importAnnotationsPretty = 
    if (importAnnotations.isEmpty) "" 
    else importAnnotations.map(_.prettyString).mkString("{", ", ", "} ")
    
  /* abstract */ def moduleName: String
}

sealed trait Unresolved extends Import {
  /* abstract */ def toNablUse: StrategoTerm
  def resolve() = Resolved(NameResolution.getModuleDef(this)
                            .getOrElse(throw BackendError(s"import $this could not be resolved")))
}

// an import is processed in multiple stages, which are (from top to bottom):
case class UnresolvedTaskId(moduleName: String, taskId: Int) extends Unresolved {
  override def toNablUse = StrategoAppl("Use", StrategoAppl("Result", StrategoInt(taskId)))
  override def prettyString = s"import $moduleName $importAnnotationsPretty// status: unresolved Task ID $taskId"
}

case class UnresolvedUri(moduleUri: VeritasModuleUri) extends Unresolved {
  override def moduleName = moduleUri.name
  override def toNablUse = moduleUri.toNablProperty
  override def prettyString = s"import $moduleName $importAnnotationsPretty// status: unresolved URI $moduleUri"
}

case class Resolved(moduleCode: Module) extends Import {
  override def moduleName = moduleCode.name
  override def prettyString = s"import $moduleName $importAnnotationsPretty// status: resolved" 
}

object Import {
  def from(term: StrategoTerm): Import = term match {
    case StrategoAppl("Import", module@StrategoString(moduleName), annos) => {
      val importAnnotations = annos match {
        case StrategoAppl("None") => Seq()
        case StrategoAppl("Some", StrategoAppl("ImportAnno", StrategoList(annos))) => annos map ImportAnnotation.from
        case t => throw VeritasParseError("Import annotation is wrong, must be either None or Some(ImportAnno([...])), got: " + t)
      }
      
      val result: Import = module.getFirstAnnotation match {
        case Some(StrategoAppl("Use", StrategoAppl("Result", StrategoInt(taskId)))) 
          => UnresolvedTaskId(moduleName, taskId)
        case Some(StrategoAppl("Use", urlTerm)) 
          => UnresolvedUri(VeritasModuleUri.fromNablDef(urlTerm))
        case t => throw VeritasParseError("Import (NaBL) annotation is wrong. Must be a NaBL Use(...) with either task ID or module URL, got: " + t)
      }
      result._importAnnotations = importAnnotations.distinct
      result
    }
    case t => throw VeritasParseError(t)
  }
}
