/*		SID: 1335894													MOD002681
 * 
 * 
 *		EXERCISE 1: Displaying required message and the current number of results received
 * 		----------------------------------------------------------------------------------
 * 
 * 		The master allocates jobs to the workers. Every result will be added to a current running total of the final value
 * 		(the parameter pi above), then the number of results/elements is raised by one, finally the current running time is
 * 		calculated (below the message, called duration). The message could be inserted after the time checking loop, this
 * 		would especially be the case if the current running time was to be shown as well.
 * 
 * 		Insertion at row: 104.
 */


import akka.actor._
import akka.routing.RoundRobinRouter

// Calculate value of Pi
object Pi extends App {
	
	// start calculation program
	// change these starting values and observe the program output changing
	calculate(nrOfWorkers = 2, nrOfElements = 1000, nrOfMessages = 1000)
	
	// PiMessage trait - acts as a sealed class
	sealed trait PiMessage
	// PiMessage Calculate object
  	case object Calculate extends PiMessage
  	// PiMessage cases
  	case class Work(start: Int, nrOfElements: Int) extends PiMessage
  	case class Result(value: Double) extends PiMessage
  	// PiApproximation case
  	case class PiApproximation(pi: Double, duration: Long)
  	
  	// worker Actor class
  	class Worker extends Actor {
		
		def calculatePiFor(start: Int, nrOfElements: Int): Double = {
			
			var acc = 0.0
			
			// Java: for (i = start; i < start + noOfElements; i++)
			for (i <- start until (start + nrOfElements))
				acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
			
			// implicit return - Double
			acc
			
		}
		
		// Actor receive method implementation
		def receive = {
			
			// receive work
			case Work(start, nrOfElements) =>
				// perform the work
				// ! = send message from case class Result
				sender ! Result(calculatePiFor(start, nrOfElements))
				
		}
	
	}
	
	// master Actor class
	class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, listener: ActorRef)
			extends Actor {
		
		// mutable variables - _ means initialisation with default value
		// all variables need to be initialised before use
		// this only works with "var", because "val" cannot be changed
		var pi: Double = _
		var nrOfResults: Int = _
		
		// immutable variable
		val start: Long = System.currentTimeMillis
		
		// implicit use of akka.actor.ActorContext - start new worker router
		val workerRouter = context.actorOf(
			Props[Worker].withRouter(
				RoundRobinRouter(
					nrOfWorkers
				)
			),
			name = "workerRouter"
		)
		
		// Actor receive method implementation
		def receive = {
			
			// calculate job - send Work signals through the workerRouter to all its Workers
			case Calculate =>
				for (i <- 0 until nrOfMessages) workerRouter ! Work(i * nrOfElements, nrOfElements)
			
			// result 
			case Result(value) =>
				// add to overall pi result
				pi += value
		
				// increase number of results
				nrOfResults += 1
	
				
				//
				// Added as per requirements to display message and the current number of results received
				//
			  println("I received part of the result!\nTotal number of results so far: " + nrOfResults + "\n")
				//
								
				
				// if number of results is the same as the number of messages issued
				if (nrOfResults == nrOfMessages) {
				
					// calculate duration
					val duration = (System.currentTimeMillis - start)
				
					// Send the result to the Listener through the PiApproximation case class
					listener ! PiApproximation(pi, duration)
				
				}
			// END CASE Result
    	}
 
	}
	
		// slave listener Actor
	class Listener extends Actor {
		
		// Actor receive method implementation
		def receive = {
			
			// display result received through the PiApproximation case class
			case PiApproximation(pi, duration) =>
				println(
					"\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s millis"
					.format(pi, duration)
				)
			
			// shut down the Actor system
			context.system.shutdown()
			
		}
		
	}
 
	// starting point
	def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
    
		// Create an Akka system
		val system = ActorSystem("PiSystem")
 
		// create the result listener, which will print the result and shutdown the system
		val listener = system.actorOf(Props[Listener], name = "listener")
 
		// create the master
		val master = system.actorOf(
			Props(
				new Master(
					nrOfWorkers, nrOfMessages, nrOfElements, listener
				)
			),
			name = "master"
		)
 
		// start the calculation
		master ! Calculate
 
	}
	
}