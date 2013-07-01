package gameengine.impl.pimps

import org.scalatest.FunSpec
import java.util.concurrent.atomic.AtomicReference

class AtomicReferencePimpsSpec extends FunSpec {
	describe("AtomicReferencePimps") {
		describe("transform") {
			it("should only run the callback once if there is no competition") {
				val obj = new Object
				val ar = new AtomicReference(obj)
				var n = 0

				ar.transform { x =>
					n += 1
					obj
				}

				assert(ar.get === obj)
				assert(n === 1)
			}

			it("should repeat until it succeeds") {
				val obj = new Object
				val newObj = new Object
				val ar = new AtomicReference(obj)
				var n = 0

				ar.transform { _ =>
					// this will change the value the first iteration (making the CAS fail),
					// but succeed the second time, since the value is unchanged this time
					ar.set(newObj)

					n += 1
					newObj
				}

				assert(ar.get === newObj)
				assert(n === 2)
			}

			it("should update the value stored") {
				val obj = new Object
				val newObj = new Object
				val ar = new AtomicReference(obj)

				ar.transform { _ =>
					newObj
				}

				assert(ar.get === newObj)
			}

			it("should pass the old value to the callback") {
				val obj = new Object
				val ar = new AtomicReference(obj)

				ar.transform { x =>
					assert(x === obj)
					x
				}
			}
		}
	}
}
