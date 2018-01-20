package util

import query._

object Parser {
  def isValid(userInput:String):Boolean = {
    var result = false
    val spacePosition = userInput.indexOf(" ")
    val action = if(spacePosition == -1) userInput else userInput.substring(0, spacePosition)
    val params = if(spacePosition == -1) "" else userInput.substring(spacePosition+1, userInput.size)
    action match{
      case "del" | "delete" => if(params.matches("(\\S+)((\\s)+)?")) result = true
      case "get" => if(params.matches("(\\S+)((\\s)+)?")) result = true
      case "help" => if(params.equals("")) result = true
      case "range" => if(params.matches("(\\S+)\\s(\\S+)((\\s)+)?")) result = true
      case "set" => if(params.matches("(\\S+)\\s(\\S+)((\\s)+)?")) result = true
      case "quit" => if(params.equals("")) result = true
      case "" =>
      case _ => print("No such command. Type \"help\" for usage info\n") 
    }
    result
  }
  
  def convertToQuery(validQueryString:String):Query = {
    var result = false
    
    val delGetPattern = "(\\S+)([\\s]*)?"
    val rangeSetPattern = "(\\S+)(\\s)(\\S+)([\\s]*)?"
    
    var query:Query = new UndefinedQuery()
    
    val spacePosition = validQueryString.indexOf(" ")
    val action = if(spacePosition == -1) validQueryString else validQueryString.substring(0, spacePosition)
    val params = if(spacePosition == -1) "" else validQueryString.substring(spacePosition+1, validQueryString.size)
    action match{
      case "del" | "delete" => {
        if(params.matches(delGetPattern)) {
          val delGetPattern.r(arg, space) = params
        }
      }
      case "get" => {
        if(params.matches(delGetPattern)){
          val delGetPattern.r(key, space) = params
          query = new GetQuery(key)
        }
      }
      case "range" => {
        if(params.matches(rangeSetPattern)){
          val rangeSetPattern.r(start,_,end,_) = params
          query = new RangeQuery(start,end)
        }
      }
      case "set" =>{
        if(params.matches(rangeSetPattern)){
          val rangeSetPattern.r(key,_,value,_) = params
          query = new SetQuery(key,value)
        }
      }
      case _ => print("Badly formatted query\n") 
    }
    query
  }
}