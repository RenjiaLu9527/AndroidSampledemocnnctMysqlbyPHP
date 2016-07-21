
<?php
 
$con = mysqli_connect("localhost","root","","testlogin");

if (!$con)
{
	die('Could not connect: ' . mysqli_error($con));
} 
$wrtORrd = $_GET['a_wORr'];





if($wrtORrd == 'write') //WRITE
{
	$comment = $_GET['a_comment'];
	$user = $_GET['a_user'];
	$time = $_GET['a_time']; 

	mysqli_query($con,'set names utf8');
	$sql = "INSERT INTO usercomment(User, Comment, Time) 
VALUES ('$user','$comment', '$time')";

	if (!mysqli_query($con,$sql))
	{	
		die('Error: '.mysqli_error($con));
	}else{
		echo 'WOK';
	}

}else if($wrtORrd == 'read') //READ
{

 
	$NowId = $_GET['a_nowid'];	//获取多少条数据

	mysqli_query($con,'set names utf8');
	//获取最新的一条数据的 umID
	$sql = "select max(umID) from usercomment";
	$result = mysqli_query($con,$sql);

	$row = mysqli_fetch_array($result,MYSQLI_BOTH);
	$MaxId = $row[0];	//zuixin
	//echo "MaxId =$MaxId";
	//【MaxId】越界判断//umID没有第0条数据
	$min = $MaxId-$NowId+1;//NowId 条数据
	if($min <= 0)
	{	$min = 0;}
		
	mysqli_query($con,'set names utf8');	
	$sqlr = "SELECT * FROM usercomment
WHERE umID>=$min AND umID<=$MaxId";

	$result = mysqli_query($con,$sqlr);

	//echo 'OK';
	while($row = mysqli_fetch_array($result,MYSQLI_ASSOC))
	{
		 //echo $row["User"].'+'.$row["Comment"].'+'.$row["Time"];
		//echo '<br>';
		 echo urlencode($row["User"]).'&'.urlencode($row["Comment"]).'&'.urlencode($row["Time"]).'/';
		//echo '<br>';
	 
	}
	echo 'ROK';//读取结束

	

}else
{
	echo 'wrtOrrdERROR';
}

	
mysqli_close($con);

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