package akka.remote.benchmark

import akka.actor.Actor
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props

// create an object based on the Receiver Class and run it straight away
object Receiver {
	
	def main(args: Array[String]): Unit = {
			
		// load receiver.conf
		val system = ActorSystem("Sys", ConfigFactory.load("receiver"))
		
		// add this actor to Props
		system.actorOf(Props[Receiver], "rcv")
  
	}

}

class Receiver extends Actor {
	
	import Sender._

	def receive = {
    	
		// sender() => Actor that sent this latest message
		case m: Echo	=> sender ! m
		
		// shutdown the ActorSystem
		case Shutdown	=> context.system.shutdown()
		
		// anything else, do nothing
		case _			=>
  
	}

}