<?php
/**
 * Created by IntelliJ IDEA.
 * User: Ben Byers
 * Date: 8/13/2014
 * Time: 7:04 PM
 */
$port = 25565;//Port we are on
$address = '8.8.8.8';//this is the address which can be set in the future to user defined
$sock = socket_create(AF_INET, SOCK_STREAM, 0);
//The first variable in create means IPv4, the second is for TCP (Full duplex), and the third is for IP.
if(!socket_connect($sock , $address , $port))
{
    $errorcode = socket_last_error();
    $errormsg = socket_strerror($errorcode);

    die("Could not connect: [$errorcode] $errormsg \n");
}