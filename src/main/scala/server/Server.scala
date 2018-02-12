import scala.collection.JavaConverters._

import java.nio.channels.SocketChannel
import java.nio.channels.ServerSocketChannel
import java.nio.channels.Selector
import java.nio.channels.SelectionKey

import java.nio.charset.UnsupportedCharsetException
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder

import java.nio.ByteBuffer

import java.io.IOException

import java.net.InetSocketAddress

import java.util.UUID

import scala.collection.mutable.Map

import configuration.Configuration
import util._
import locking._

object Server {
  
  private val DEFAULT_PORT = Configuration.defaultPort
  
  private val resultMap: Map[UUID,String] = Map()
  private val processor:Processor = Processor(getQueryResult)
  
	private var decoder:CharsetDecoder = null;
	private var messageCharset:Charset = null;
	
	private def getQueryResult(keyID:UUID, result:String):Unit = {
	  resultMap += keyID -> result
	}
  
  def main(args:Array[String]):Unit = {
    
    var serverChannel:ServerSocketChannel = null
		var selector:Selector = null
    
		try {
		  // Throws UnsupportedCharsetException
			messageCharset = Charset.forName("US-ASCII")
		  decoder = messageCharset.newDecoder();
			
		  selector = Selector.open();
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(new InetSocketAddress(DEFAULT_PORT));
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			
		} catch{
		  case e:UnsupportedCharsetException => System.err.println("Cannot create charset for this application. Exiting...")
		                                         System.exit(1)
	  }
		println("Server running")
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
			    if(key.isAcceptable()){
						var client:SocketChannel = null
						try {
							client = key.channel().asInstanceOf[ServerSocketChannel].accept()
							client.configureBlocking(false);
							client.register(selector, SelectionKey.OP_READ)
							
							key.attach(UUID.randomUUID())

						} catch{
						  case e:IOException => println("Error accepting connection")
						}
						println("Client accepted")

					}
			    if(key.isReadable()){
			      val buffer = ByteBuffer.allocate(2048)
			      key.channel().asInstanceOf[SocketChannel].read(buffer)
			      buffer.flip()
			      val charBuf = decoder.decode(buffer)
			      println("Bytes read: " + charBuf)
			      
            processor.processQuery(Parser.convertToQuery(charBuf.toString()), key.attachment().asInstanceOf[UUID])
			      key.interestOps(SelectionKey.OP_WRITE)
					}
			    if(key.isWritable()){
			      val queryResult = resultMap.remove(key.attachment().asInstanceOf[UUID]).getOrElse(null)
			      if(queryResult != null){
  			      val commandBytes:Array[Byte] = queryResult.getBytes(messageCharset);
              val byteBuffer = ByteBuffer.allocate(commandBytes.length) 
              byteBuffer.put(commandBytes)
              byteBuffer.flip()
          	  key.channel().asInstanceOf[SocketChannel].write(byteBuffer)
  			      key.interestOps(SelectionKey.OP_READ)
			      }
			    }
			  }
				selectedKeysList.clear()
			}
		}
  }
}