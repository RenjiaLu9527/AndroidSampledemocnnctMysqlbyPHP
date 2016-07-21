<?php
$LGN_IN_RGST = $_GET['a_lgninout'];//denglu-IN OR zhuce-RGST
$name = $_GET['a_name'];
$psw = $_GET['a_psw'];
 
$con = mysqli_connect("localhost","root","","testlogin");

if (!$con)
{
	//die('Could not connect: ' . mysqli_error($con));
	die ('ERROR&服务器连接失败 请重试');
} else{
	if($LGN_IN_RGST == "IN")
	{	
	//IN
	mysqli_query($con,'set names utf8');
	$sql = "select * from users WHERE name = '$name'";
	$result = mysqli_query($con,$sql);
	//name 查询不存在
	if(!$result)
	{
		die ('ERROR&此用户名没有注册');
	}
	$row = mysqli_fetch_array($result,MYSQLI_BOTH);
	
	//比较 name对应的 psw
	if($psw == $row[2])
	{
		echo 'OK&LOGINOK';
	}else{
		die('ERROR&密码不正确');
		 
	}
	}//IN -end

//
//
	else
	{
	//RGST

	//获取最新的一条数据的 umID
	$sql = "select max(id) from users";
	$result = mysqli_query($con,$sql);
	$row = mysqli_fetch_array($result,MYSQLI_BOTH);
	$MaxId = $row[0];	//zuixin

	//$name 附上当前id,使其独一无二
	$name = $name.''.$MaxId;

	mysqli_query($con,'set names utf8');
	$sql = "INSERT INTO users(name, psw) 
VALUES ('$name','$psw')";

	if (!mysqli_query($con,$sql))
	{
		
		die('ERROR&注册失败 请联系管理员');
	}else{
		//返回OK和最大ID,更新用户名控件值
		echo 'OK&'."$name";
	}
	}//RGST -end
}

mysqli_close($con);
?>