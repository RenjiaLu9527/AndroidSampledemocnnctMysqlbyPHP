 
<?php
$mysqlname = $_GET['name'];
$mysqlpsw = $_GET['psw'];

$con = mysqli_connect("localhost",$mysqlname,$mysqlpsw,"testlogin");

if (!$con)
{
	//die('Could not connect: ' . mysql_error());
	echo "ERROR";
}else
{
	echo  "OK";
}
mysqli_close($con);

function urlsafe_b64encode($string) {
   $data = base64_encode($string);
   $data = str_replace(array('+','/','='),array('-','_',''),$data);
   return $data;
 }


function characet($data){
  if( !empty($data) ){    
    $fileType = mb_detect_encoding($data , array('UTF-8','GBK','LATIN1','BIG5')) ;   
    if( $fileType != 'UTF-8'){   
      $data = mb_convert_encoding($data ,'utf-8' , $fileType);   
    }   
  }   
  return $data;    
}
?>

 