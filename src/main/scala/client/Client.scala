import scala.collection.JavaConverters._

import configuration.Configuration

import java.nio.ByteBuffer

import java.nio.channels.SocketChannel
import java.nio.channels.Selector
import java.nio.channels.SelectionKey

import java.nio.charset.UnsupportedCharsetException
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder

import java.io.IOException

import java.net.InetSocketAddress

import scala.util.matching.Regex

import scala.util.control.Breaks

object Client {
  
  private val LOCALHOST = Configuration.defaultHost
  private val DEFAULT_PORT = Configuration.defaultPort
  
	private var decoder:CharsetDecoder = null;
	private var messageCharset:Charset = null;
	
	private var command = "" 
	
	private def userPrompt(f:String => Unit):Unit  = {
	  var userInput = ""
	  while(!userInput.equals("quit")){
	     var validCommand = false
       userInput = scala.io.StdIn.readLine()
       val spacePosition = userInput.indexOf(" ")
       val action = if(spacePosition == -1) userInput else userInput.substring(0, spacePosition)
       val params = if(spacePosition == -1) "" else userInput.substring(spacePosition+1, userInput.size)
       action match{
         case "del" | "delete" => if(params.matches("(\\S+)((\\s)+)?")) validCommand = true
         case "get" => if(params.matches("(\\S+)((\\s)+)?")) validCommand = true
         case "help" =>  userInput = "help"; validCommand = true
         case "range" => if(params.matches("(\\S+)\\s(\\S+)((\\s)+)?")) validCommand = true
         case "set" => if(params.matches("(\\S+)\\s(\\S+)((\\s)+)?")) validCommand = true
         case "quit" => userInput = "quit"; validCommand = true
         case "" =>
         case _ => print("No such command. Type \"help\" for usage info\n") 
       }
       if(validCommand) f(userInput.trim())
     }
	}
	
	private def setCommand(userInput:String):Unit = {
	  command.synchronized {
	    command = userInput;
	  }
	}
	
	private def shouldQuit():Boolean = {
	  command.synchronized {
	    command.equals("quit")
	  }
	}
	
	private def userInputAvailable():Boolean = {
	  command.synchronized {
	    !command.equals("")
	  }
	}
	
	private def sendUserInput(key:SelectionKey):Unit = {
    var commandBytes:Array[Byte] = null
    command.synchronized{
      commandBytes = command.getBytes(messageCharset);
    }
    val byteBuffer = ByteBuffer.allocate(2048) 
    byteBuffer.put(commandBytes)
    byteBuffer.flip()
    println("Sending: " + command)
	  key.channel().asInstanceOf[SocketChannel].write(byteBuffer)
	  command = ""
	}
  
  def main(args:Array[String]):Unit = {
    
    val userInputThread = new Thread{
      override def run{
        userPrompt(setCommand)
      }
    }
    
    var clientChannel:SocketChannel = null
		var remoteAddress:InetSocketAddress = null
		var selector:Selector = null
    
//		try {
		  // Throws UnsupportedCharsetException
			messageCharset = Charset.forName("US-ASCII")
		  decoder = messageCharset.newDecoder();
			
			// Throws IllegalArgumentException or SecurityException
			remoteAddress = new InetSocketAddress(LOCALHOST, DEFAULT_PORT)
			
			// Throws IOException
			selector = Selector.open();
			
			// Throws IOException
			clientChannel = SocketChannel.open();
			clientChannel.configureBlocking(false);
			clientChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			clientChannel.connect(remoteAddress);
		/*} catch{
		  
		  case e:Exception => e.printStackTrace(); System.exit(1)
		  
		  case e:IOException => {}//TODO
		  case e:UnsupportedCharsetException => System.err.println("Cannot create charset for this application. Exiting...")
		                                        System.exit(1)
		  case e:Exception => e match{
		    case _:IllegalArgumentException | _:SecurityException => {}//TODO
		  }
	  }*/
		
		userInputThread.start()
		
		val loopBreaker = new Breaks
		loopBreaker.breakable{
  		while(true){
  		  var selectedKeys = 0
  		  try {
  				selectedKeys = selector.select()
  			}catch{
  			  case e:IOException => {}
  			}
  			if(selectedKeys > 0){
  			  
  			  val selectedKeysList = selector.selectedKeys().asScala
  			  
  			  for(key <- selectedKeysList){
  			    if(key.isConnectable()){
  						key.channel().asInstanceOf[SocketChannel].finishConnect()
  						
  					}
  			    if(key.isReadable()){
  			      val buffer = ByteBuffer.allocate(2048)
			        key.channel().asInstanceOf[SocketChannel].read(buffer)
  			      buffer.flip()
  			      val charBuf = decoder.decode(buffer)
  			      println("Bytes read: " + charBuf)
  					  key.interestOps(SelectionKey.OP_WRITE)
  					}
  			    
  			    if(key.isWritable()){
  			      if(userInputAvailable()){
  			        sendUserInput(key)
  			        key.interestOps(SelectionKey.OP_READ)
  			      }
  			    }
  			    
  			    if(shouldQuit()){
  			      println("Should quit")
  			      key.channel().close()
  			      userInputThread.join()
  			      loopBreaker.break()
  			    }
  			  }
  				selectedKeysList.clear()
  			}
  		}
		}
  }
}