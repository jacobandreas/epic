package scalanlp.parser.epic

import scalanlp.parser.Rule
import scalanlp.util.CachedHashCode
import scalala.tensor.Counter

/**
 * 
 * @author dlwh
 */
trait Feature;

// Meta Features
/** conjoins some features */
case class SequenceFeature(f: Seq[Feature]) extends Feature with CachedHashCode
/** Associate a symbol with a feature */
case class TaggedFeature(f: Feature, symbol: Symbol) extends Feature with CachedHashCode

// Parer Features
/** A Rule feature is just an indicator on there being this rule */
case class RuleFeature[L](r: Rule[L]) extends Feature with CachedHashCode
/** A simple indicator feature */
case class WeightedFeature(kind: Symbol) extends Feature with CachedHashCode
/** A Lexical feature is just an indicator on there being this word */
case class LexicalFeature[L,W](l: L, w: W) extends Feature with CachedHashCode
/** wraps a feature with substate information */
case class SubstateFeature[T](f: Feature, states: Seq[T]) extends Feature with CachedHashCode

case class IndicatorFeature(a: Any) extends Feature with CachedHashCode

