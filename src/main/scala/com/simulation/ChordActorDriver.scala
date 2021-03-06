package com.simulation

import scala.language.postfixOps
import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.remote.transport.ActorTransportAdapter.AskTimeout
import com.simulation.actors.chord.servers.ServerActor
import com.simulation.actors.chord.supervisors.SupervisorActor
import akka.util.Timeout
import com.simulation.actors.chord.supervisors.SupervisorActor.{createServerActor, getDataSupervisor, getSnapshot, removeNodeSupervisor}
import com.simulation.actors.chord.users.UserActor
import com.simulation.actors.chord.users.UserActor.{createUserActor, getDataUserActor}
import com.simulation.beans.EntityDefinition
import com.simulation.utils.FingerActor
import com.simulation.utils.Utility.getMoviesData
import com.typesafe.config.ConfigFactory
import org.ddahl.rscala.RClient
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.util.Random

/**
 * As the name suggests, routes the requests to the appropriate services.
 */
object ChordActorDriver {

  private val conf = ConfigFactory.load("application.conf")

  val numUsers = conf.getInt("num_of_users")
  val numNodes = conf.getInt("num_of_nodes")

  val actorSystem = ActorSystem("actorSystem")

  val serverActor = actorSystem.actorOf(Props(new ServerActor(1, numNodes)), "server_actor")
  val userActor = actorSystem.actorOf(Props(new UserActor(1, actorSystem)), "user_actor")
  val supervisorActor = actorSystem.actorOf(Props(new SupervisorActor(1, numNodes, actorSystem)),"supervisor_actor")
  val fingerActor = actorSystem.actorOf(Props(new FingerActor()),"finger_actor")

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  List.tabulate(numUsers)(i => userActor ! createUserActor(i))

  val movieData = getMoviesData
  var serverActorCount = 0

  val timeout = Timeout(10 seconds)

  //val shard = ServerActor.startMerchantSharding(actorSystem, 1, numNodes)
  def createServerNode(): Int = {
    if(numNodes > serverActorCount) {
      val nodeIndex = supervisorActor ? createServerActor()
      val nodeIndexR = Await.result(nodeIndex, timeout.duration).asInstanceOf[Int]
      serverActorCount += 1
      return nodeIndexR
    }
    -1
  }

  def removeNode(nodeIndex:Int): Boolean = {
    val nodeRemoved = supervisorActor ? removeNodeSupervisor(nodeIndex)
    val nodeRemovedR = Await.result(nodeRemoved, timeout.duration).asInstanceOf[Boolean]
    if(nodeRemovedR)
      serverActorCount -= 1
    nodeRemovedR
  }

  def loadData(id: Int): String = {
    logger.info("In loadData driver")
    val userActorId = 1
    val dataHandlerActor = actorSystem.actorSelection("akka://actorSystem/user/user_actor/"+userActorId)
    val resultFuture  = dataHandlerActor ? UserActor.loadData(movieData(id))
    val result = Await.result(resultFuture, timeout.duration)
    result.toString
  }

  def getData(id: Int): Any = {
    val dataHandlerActor = actorSystem.actorSelection("akka://actorSystem/user/user_actor/1")
    val dataRetrieved = dataHandlerActor ? getDataUserActor(id)
    val result = Await.result(dataRetrieved, timeout.duration)
    result
  }

  def printSnapshot(): Any = {
    logger.info("Print Snapshot Driver")
    val snapshotRetrieved = supervisorActor ? getSnapshot()
    val result = Await.result(snapshotRetrieved, timeout.duration)
    result
  }

}
