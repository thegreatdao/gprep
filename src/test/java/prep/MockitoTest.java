package prep;
/*
 * http://docs.mockito.googlecode.com/hg/org/mockito/Mockito.html#1
 */
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class MockitoTest
{
	@Mock
	private List<String> annotationCreatedMock;
	private List<String> mockedList;
	
	@BeforeClass
	public static void init()
	{
		 MockitoAnnotations.initMocks(MockitoTest.class);
	}
	
	@Before
	public void  setUp()
	{
		mockedList = mock(List.class);
	}
	
	@Test
	public void testMockAnnotation()
	{
		// using mock object
		annotationCreatedMock.add("one");
		annotationCreatedMock.clear();

		// verification
		verify(annotationCreatedMock).add("one");
		verify(annotationCreatedMock).clear();
	}
	
	@Test
	public void testMock()
	{
		// using mock object
		mockedList.add("one");
		mockedList.clear();
		
		// verification
		verify(mockedList).add("one");
		verify(mockedList).clear();
	}

	@Test(expected = RuntimeException.class)
	public void testStubbing()
	{
		/*
		 *  You can mock concrete classes, not only interfaces
		 *LinkedList<String> mockedList = mock(LinkedList.class);
		 */

		// stubbing
		when(mockedList.get(0)).thenReturn("first");
		when(mockedList.get(1)).thenThrow(new RuntimeException());

		// following prints "first"
		System.out.println(mockedList.get(0));

		// following throws runtime exception
		System.out.println(mockedList.get(1));

		// following prints "null" because get(999) was not stubbed
		System.out.println(mockedList.get(999));

		// Although it is possible to verify a stubbed invocation, usually it's
		// just redundant
		// If your code cares what get(0) returns then something else breaks
		// (often before even verify() gets executed).
		// If your code doesn't care what get(0) returns then it should not be
		// stubbed. Not convinced? See here.
		verify(mockedList).get(0);

	}

	@Test
	public void testArgMatcher()
	{
		// stubbing using built-in anyInt() argument matcher
		when(mockedList.get(anyInt())).thenReturn("element");

		// following prints "element"
		System.out.println(mockedList.get(999));

		// you can also verify using an argument matcher
		verify(mockedList).get(anyInt());

	}

	@Test
	public void testExactNumberOfCalls()
	{
		// using mock
		mockedList.add("once");

		mockedList.add("twice");
		mockedList.add("twice");

		mockedList.add("three times");
		mockedList.add("three times");
		mockedList.add("three times");

		// following two verifications work exactly the same - times(1) is used
		// by default
		verify(mockedList).add("once");
		verify(mockedList, times(1)).add("once");

		// exact number of invocations verification
		verify(mockedList, times(2)).add("twice");
		verify(mockedList, times(3)).add("three times");

		// verification using never(). never() is an alias to times(0)
		verify(mockedList, never()).add("never happened");

		// verification using atLeast()/atMost()
		verify(mockedList, atLeastOnce()).add("three times");
		// verify(mockedList, atLeast(2)).add("five times");// return exception
		verify(mockedList, atMost(5)).add("three times");
	}

	@Test(expected = RuntimeException.class)
	public void testExceptionWithVoidMethod()
	{
		doThrow(new RuntimeException()).when(mockedList).clear();

		// following throws RuntimeException:
		mockedList.clear();

	}

	@Test
	public void testVerificationInOrder()
	{
		List firstMock = mock(List.class);
		List secondMock = mock(List.class);

		// using mocks
		firstMock.add("was called first");
		secondMock.add("was called second");

		// create inOrder object passing any mocks that need to be verified in
		// order
		InOrder inOrder = inOrder(firstMock, secondMock);

		// following will make sure that firstMock was called before secondMock
		inOrder.verify(firstMock).add("was called first");
		inOrder.verify(secondMock).add("was called second");

	}

	@Test
	public void testMockInteraction()
	{
		List firstMock = mock(List.class);
		List secondMock = mock(List.class);
		// using mocks - only mockOne is interacted
		firstMock.add("one");

		// ordinary verification
		verify(firstMock).add("one");

		// verify that method was never called on a mock
		verify(firstMock, never()).add("two");

		// verify that other mocks were not interacted
		verifyZeroInteractions(firstMock, secondMock);
	}
	
	@Test //don't use this often
	public void testRedundantInteraction()
	{
		 //using mocks
		 mockedList.add("one");
		 mockedList.add("two");
		 
		 verify(mockedList).add("one");
		 verify(mockedList).add("two");
		 
		 //following verification will not fail 
		 verifyNoMoreInteractions(mockedList);

	}
	
	@Test(expected = RuntimeException.class)
	public void testStubConsecutiveCalls()
	{
		when(mockedList.get(anyInt())).thenThrow(new RuntimeException()).thenReturn("one", "two", "three");
		mockedList.get(-1);
		String string = mockedList.get(-1);
		assertEquals("one", string);
		string = mockedList.get(-1);
		assertEquals("two", string);
		string = mockedList.get(-1);
		assertEquals("three", string);
	}
	
	@Test // controversial
	public void testStubWithCallback()
	{
		 when(mockedList.get(anyInt())).thenAnswer(new Answer<String>() {
		     public String answer(InvocationOnMock invocation) {
		         Object[] args = invocation.getArguments();
//		         Object mock = invocation.getMock();
		         return "called with arguments: " + args;
		     }
		 });
		 System.out.println(mockedList.get(1));
	}
	
	@Test(expected = RuntimeException.class)
	public void testStubVoid()
	{
		doThrow(new RuntimeException()).when(mockedList).clear();
		mockedList.clear();
		doReturn("doReturn from void").when(mockedList).clear();
	}
	
	@Test
	public void testSpyingObject()
	{
		List<String> list = new LinkedList<String>();
		List<String> spy = spy(list);

		// optionally, you can stub out some methods:
		when(spy.size()).thenReturn(100);

		// using the spy calls real methods
		spy.add("one");
		spy.add("two");

		// prints "one" - the first element of a list
		System.out.println(spy.get(0));

		// size() method was stubbed - 100 is printed
		System.out.println(spy.size());

		// optionally, you can verify
		verify(spy).add("one");
		verify(spy).add("two");

	}
	
	@Test(expected = IndexOutOfBoundsException.class)
	public void testSpyGotchas()
	{
		List<String> list = new LinkedList<String>();
		List<String> spy = spy(list);

		// Impossible: real method is called so spy.get(0) throws
		// IndexOutOfBoundsException (the list is yet empty)
		when(spy.get(0)).thenReturn("foo");

		// You have to use doReturn() for stubbing
		String string = doReturn("foo").when(spy).get(0);
		assertEquals("foo", string);
	}
}

